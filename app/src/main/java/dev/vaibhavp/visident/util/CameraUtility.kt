package dev.vaibhavp.visident.util

import android.content.Context
import android.net.Uri
import android.util.Log
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
import java.io.File
import java.util.UUID

object CameraUtility {

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
                Log.e("CameraUtility", "Use case binding failed", exc)
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
                    Log.e("CameraUtility", "Photo capture failed: ${exc.message}", exc)
                    onError(exc)
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val savedUri = Uri.fromFile(outputFile)
                    Log.d("CameraUtility", "Photo capture succeeded: $savedUri")
                    onImageSaved(savedUri)
                }
            }
        )
    }
}
