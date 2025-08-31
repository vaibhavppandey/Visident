package dev.vaibhavp.visident.viewmodel

import android.content.Context
import android.net.Uri
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.core.SurfaceRequest
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.vaibhavp.visident.repo.SessionRepository
import dev.vaibhavp.visident.util.CameraUtility
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class SessionViewModel @Inject constructor(
    private val repository: SessionRepository
) : ViewModel() {

    private val _surfaceRequest = MutableStateFlow<SurfaceRequest?>(null)
    val surfaceRequest: StateFlow<SurfaceRequest?> = _surfaceRequest.asStateFlow()

    private val _capturedImageUri = MutableStateFlow<Uri?>(null)
    val capturedImageUri: StateFlow<Uri?> = _capturedImageUri.asStateFlow()

    private val _pictureCount = MutableStateFlow(0)
    val pictureCount: StateFlow<Int> = _pictureCount.asStateFlow()

    private val cameraPreviewUseCase = Preview.Builder().build().apply {
        setSurfaceProvider { newSurfaceRequest ->
            _surfaceRequest.update { newSurfaceRequest }
        }
    }
    private val imageCaptureUseCase = ImageCapture.Builder().build()

    fun bindToCamera(appContext: Context, lifecycleOwner: LifecycleOwner) {
        viewModelScope.launch {
            val success = CameraUtility.bindCameraUseCases(
                context = appContext,
                lifecycleOwner = lifecycleOwner,
                previewUseCase = cameraPreviewUseCase,
                imageCaptureUseCase = imageCaptureUseCase
            )
            if (!success) {
                _surfaceRequest.update { null }
            }
        }
    }

    fun takePicture(context: Context) {
        viewModelScope.launch {
            val outputFile: File = repository.createTempImageFile()
            CameraUtility.takePicture(
                context = context,
                imageCaptureUseCase = imageCaptureUseCase,
                outputFile = outputFile,
                onImageSaved = { uri ->
                    if (uri != null) {
                        _capturedImageUri.update { uri }
                        _pictureCount.update { it + 1 }
                    } else {
                        _capturedImageUri.update { null }
                    }
                },
                onError = {
                    _capturedImageUri.update { null }
                }
            )
        }
    }

    fun clearCapturedImageUri() {
        _capturedImageUri.update { null }
    }

    fun moveCachedImagesToSession(sessionId: String) {
        repository.moveCachedImagesToSession(sessionId)
    }

    fun clearCache() {
        repository.clearCache()
    }
}
