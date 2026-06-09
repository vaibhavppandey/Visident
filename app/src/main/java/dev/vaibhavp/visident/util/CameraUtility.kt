package dev.vaibhavp.visident.util

import android.content.Context
import android.net.Uri
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.core.content.ContextCompat
import timber.log.Timber
import java.io.File

object CameraUtility {

    private val timber = Timber.tag("CameraUtility")

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
