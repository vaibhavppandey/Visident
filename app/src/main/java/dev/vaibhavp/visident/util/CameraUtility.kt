package dev.vaibhavp.visident.util

import android.content.Context
import android.net.Uri
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.lifecycle.awaitInstance
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File

object CameraUtility {

    private val timber = Timber.tag("CameraUtility")

    suspend fun bindCameraUseCases(
        context: Context,
        lifecycleOwner: LifecycleOwner,
        previewUseCase: Preview,
        imageCaptureUseCase: ImageCapture,
        cameraSelector: CameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
    ): Boolean {
        return withContext(Dispatchers.Main) {
            try {
                val cameraProvider = ProcessCameraProvider.awaitInstance(context)
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    previewUseCase,
                    imageCaptureUseCase
                )
                true
            } catch (exc: Exception) {
                timber.e(exc, "Use case binding failed")
                false
            }
        }
    }

    fun takePicture(
        context: Context,
        imageCaptureUseCase: ImageCapture,
        outputFile: File,
        onImageSaved: (Uri?) -> Unit,
        onError: (ImageCaptureException) -> Unit
    ) {
        val outputOptions = ImageCapture.OutputFileOptions
            .Builder(outputFile)
            .build()

        imageCaptureUseCase.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    timber.e(exc, "Photo capture failed: ${exc.message}")
                    onError(exc)
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val savedUri = Uri.fromFile(outputFile)
                    timber.d( "Photo capture succeeded: $savedUri")
                    onImageSaved(savedUri)
                }
            }
        )
    }
}
