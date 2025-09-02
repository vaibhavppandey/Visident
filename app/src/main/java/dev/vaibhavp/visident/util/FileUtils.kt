package dev.vaibhavp.visident.util

import android.content.Context
import timber.log.Timber
import java.io.File
import java.io.IOException

object FileUtils {

    private fun getAppFolder(context: Context): File {
        val appSpecificMediaDir = context.externalMediaDirs.firstOrNull()
            ?: throw IOException("External media storage not available. Please ensure storage permission is granted.")
        val sessionsDir = File(appSpecificMediaDir, "Sessions")
        if (!sessionsDir.exists()) {
            sessionsDir.mkdirs()
        }
        return sessionsDir
    }

    fun createSessionFolder(context: Context, sessionId: String): File {
        val appSessionsBaseFolder = getAppFolder(context)
        val sessionFolder = File(appSessionsBaseFolder, sessionId)
        if (!sessionFolder.exists()) {
            sessionFolder.mkdirs()
        }
        return sessionFolder
    }

    fun getSessionImages(context: Context, sessionId: String): List<File> {
        val folder = createSessionFolder(context, sessionId)
        // only imazes
        return folder.listFiles { file ->
            file.isFile && (file.name.endsWith(".jpg", true) ||
                    file.name.endsWith(".jpeg", true) ||
                    file.name.endsWith(".png", true))
        }?.toList() ?: emptyList()
    }

    private fun getCacheFolder(context: Context): File {
        val cacheFolder = File(context.cacheDir, "temp")
        if (!cacheFolder.exists()) cacheFolder.mkdirs()
        return cacheFolder
    }

    fun createTempImageFile(context: Context): File {
        val cacheFolder = getCacheFolder(context)
        val timeStamp = System.currentTimeMillis()
        return File(cacheFolder, "IMG_${timeStamp}.jpg")
    }

    fun getCachedImages(context: Context): List<File> {
        val cacheFolder = getCacheFolder(context)
        return cacheFolder.listFiles { file ->
            file.isFile && (file.name.endsWith(".jpg", true) ||
                    file.name.endsWith(".jpeg", true) ||
                    file.name.endsWith(".png", true))
        }?.toList() ?: emptyList()
    }

    fun clearCache(context: Context) {
        val cacheFolder = getCacheFolder(context)
        cacheFolder.listFiles()?.forEach { file ->
            if (file.isFile) {
                file.delete()
            }
        }
    }

    fun moveCachedImagesToSession(context: Context, sessionId: String) {
        val cachedImages = getCachedImages(context)
        if (cachedImages.isEmpty()) return

        val sessionFolder = createSessionFolder(context, sessionId)

        cachedImages.forEach { file ->
            val targetFile = File(sessionFolder, file.name)
            try {
                if (!file.renameTo(targetFile)) {
                    file.copyTo(targetFile, overwrite = true)
                    file.delete()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Timber.e(e)
            }
        }
        clearCache(context)
    }
}
