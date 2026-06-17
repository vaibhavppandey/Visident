package dev.vaibhavp.visident.viewmodel

import android.content.Context
import androidx.camera.core.Camera
import androidx.camera.core.CameraControl
import androidx.camera.core.CameraSelector
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.core.SurfaceOrientedMeteringPointFactory
import androidx.camera.core.SurfaceRequest
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.lifecycle.awaitInstance
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.vaibhavp.visident.data.model.SessionEntity
import dev.vaibhavp.visident.repo.SessionRepository
import dev.vaibhavp.visident.util.CameraUtility
import androidx.compose.ui.geometry.Offset
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File
import javax.inject.Inject

/** Owns the camera session: preview binding, capture, flash/lens/focus controls, and finalize. */
@HiltViewModel
class CaptureViewModel @Inject constructor(
    private val repository: SessionRepository,
) : ViewModel() {

    private val _surfaceRequest = MutableStateFlow<SurfaceRequest?>(null)
    val surfaceRequest: StateFlow<SurfaceRequest?> = _surfaceRequest.asStateFlow()

    private val _pictureCount = MutableStateFlow(0)
    val pictureCount: StateFlow<Int> = _pictureCount.asStateFlow()

    private val _flashEnabled = MutableStateFlow(false)
    val flashEnabled: StateFlow<Boolean> = _flashEnabled.asStateFlow()

    private val _hasFlashUnit = MutableStateFlow(false)
    val hasFlashUnit: StateFlow<Boolean> = _hasFlashUnit.asStateFlow()

    // Changing this re-keys the binding effect in the screen, which rebinds with the new lens.
    private val _lensFacing = MutableStateFlow(CameraSelector.DEFAULT_BACK_CAMERA)
    val lensFacing: StateFlow<CameraSelector> = _lensFacing.asStateFlow()

    private val _captureError = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val captureError: SharedFlow<String> = _captureError.asSharedFlow()

    @Volatile private var cameraControl: CameraControl? = null
    @Volatile private var meteringPointFactory: SurfaceOrientedMeteringPointFactory? = null

    private val imageCapture: ImageCapture = ImageCapture.Builder()
        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
        .build()

    private val previewUseCase = Preview.Builder().build().apply {
        setSurfaceProvider { newSurfaceRequest ->
            _surfaceRequest.update { newSurfaceRequest }
            // A metering-point factory in the preview buffer's coordinate space, for tap-to-focus.
            meteringPointFactory = SurfaceOrientedMeteringPointFactory(
                newSurfaceRequest.resolution.width.toFloat(),
                newSurfaceRequest.resolution.height.toFloat(),
            )
        }
    }

    /**
     * Binds preview + capture to [lifecycleOwner] using the current lens, then suspends until the
     * caller's scope is cancelled (e.g. leaving the screen or switching lens), unbinding on exit.
     * Call from a [lensFacing]-keyed LaunchedEffect so switching the lens rebinds.
     */
    suspend fun bindToCamera(appContext: Context, lifecycleOwner: LifecycleOwner) {
        val provider = try {
            ProcessCameraProvider.awaitInstance(appContext)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Timber.e(e, "Failed to obtain camera provider")
            _surfaceRequest.update { null }
            return
        }
        try {
            provider.unbindAll()
            val camera: Camera = provider.bindToLifecycle(
                lifecycleOwner,
                _lensFacing.value,
                previewUseCase,
                imageCapture,
            )
            cameraControl = camera.cameraControl
            _hasFlashUnit.value = camera.cameraInfo.hasFlashUnit()
            // A camera with no flash unit (e.g. most front cameras) must not stay FLASH_MODE_ON,
            // especially as the flash toggle is then hidden and could not be turned back off.
            if (!_hasFlashUnit.value) _flashEnabled.value = false
            applyFlashMode()
            awaitCancellation()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Timber.e(e, "Camera use case binding failed")
            _surfaceRequest.update { null }
        } finally {
            provider.unbindAll()
            cameraControl = null
        }
    }

    fun toggleLens() {
        _lensFacing.value =
            if (_lensFacing.value == CameraSelector.DEFAULT_BACK_CAMERA) {
                CameraSelector.DEFAULT_FRONT_CAMERA
            } else {
                CameraSelector.DEFAULT_BACK_CAMERA
            }
    }

    fun toggleFlash() {
        _flashEnabled.update { !it }
        applyFlashMode()
    }

    private fun applyFlashMode() {
        imageCapture.flashMode =
            if (_flashEnabled.value) ImageCapture.FLASH_MODE_ON else ImageCapture.FLASH_MODE_OFF
    }

    /** [bufferOffset] must already be transformed into preview-buffer coordinates by the viewfinder. */
    fun focusOnPoint(bufferOffset: Offset) {
        val factory = meteringPointFactory ?: return
        val control = cameraControl ?: return
        val point = factory.createPoint(bufferOffset.x, bufferOffset.y)
        control.startFocusAndMetering(FocusMeteringAction.Builder(point).build())
    }

    fun takePicture(context: Context) {
        viewModelScope.launch {
            val outputFile: File = repository.createTempImageFile()
            CameraUtility.takePicture(
                context = context,
                imageCaptureUseCase = imageCapture,
                outputFile = outputFile,
                onImageSaved = { uri -> if (uri != null) _pictureCount.update { it + 1 } },
                onError = { exc -> _captureError.tryEmit(exc.message ?: "Couldn't capture photo") },
            )
        }
    }

    fun finalizeSession(
        sessionId: String,
        name: String,
        age: Int,
        imageCount: Int,
        onComplete: () -> Unit,
        onError: (String) -> Unit,
    ) {
        viewModelScope.launch {
            try {
                repository.saveSession(
                    SessionEntity(sessionId = sessionId, name = name, age = age, imageCount = imageCount),
                )
                // Moves each cached photo into the session folder, deleting sources only on success.
                repository.moveCachedImagesToSession(sessionId)
                _pictureCount.update { 0 }
                onComplete()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Timber.e(e, "Failed to finalize session")
                onError(e.message ?: "Couldn't save session")
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        // Leaving the capture flow without finalizing (back arrow / system back) must not leave
        // captured photos in the shared cache, or they would be swept into the next session.
        // viewModelScope is already cancelled here, so clear on an independent IO scope.
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            runCatching { repository.clearCache() }
        }
    }
}
