package dev.vaibhavp.visident.viewmodel

import android.content.Context
import android.net.Uri
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.core.SurfaceRequest
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.vaibhavp.visident.util.CameraUtility
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SessionViewModel : ViewModel() {
    private val _surfaceRequest = MutableStateFlow<SurfaceRequest?>(null)
    val surfaceRequest: StateFlow<SurfaceRequest?> = _surfaceRequest

    private val _capturedImageUri = MutableStateFlow<Uri?>(null)
    val capturedImageUri: StateFlow<Uri?> = _capturedImageUri

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
                // TODO: front + back camera func
            )
            if (!success) {
                _surfaceRequest.update { null }
            }
        }
    }

    fun takePicture(context: Context) {
        viewModelScope.launch { 
            CameraUtility.takePicture(
                context = context,
                imageCaptureUseCase = imageCaptureUseCase,
                onImageSaved = { uri ->
                    _capturedImageUri.update { uri }
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
}
