package dev.vaibhavp.visident.util

import android.content.Context
import java.io.File
import java.io.IOException


object FileUtils {

    private fun getAppFolder(context: Context): File {
        val externalMediaDir = context.externalMediaDirs.firstOrNull()
            ?: throw IOException("External media storage not available. Please ensure storage permission is granted.")
        val baseDir = externalMediaDir.parentFile
            ?: throw IOException("Cannot access base media directory from ${externalMediaDir.absolutePath}.")
        return File(baseDir, "Visident/Sessions")
    }

    fun createSessionFolder(context: Context, sessionId: String): File {
        val appFolder = getAppFolder(context)
        val sessionFolder = File(appFolder, sessionId)
        if (!sessionFolder.exists()) sessionFolder.mkdirs()
        return sessionFolder
    }

    fun getSessionImages(context: Context, sessionId: String): List<File> {
        val folder = createSessionFolder(context, sessionId)
        return folder.listFiles()?.toList() ?: emptyList()
    }

    private fun getCacheFolder(context: Context): File {
        val cacheFolder = File(context.cacheDir, "session_temp")
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
        return cacheFolder.listFiles()?.toList() ?: emptyList()
    }

    fun clearCache(context: Context) {
        val cacheFolder = getCacheFolder(context)
        cacheFolder.listFiles()?.forEach { it.delete() }
    }

    fun moveCachedImagesToSession(context: Context, sessionId: String) {
        val cachedImages = getCachedImages(context)
        if (cachedImages.isEmpty()) return

        val sessionFolder = createSessionFolder(context, sessionId)

        cachedImages.forEach { file ->
            val targetFile = File(sessionFolder, file.name)
            try {
                // Try moving first
                if (!file.renameTo(targetFile)) {
                    // If rename fails (cross-filesystem), fallback to copy + delete
                    file.copyTo(targetFile, overwrite = true)
                    file.delete()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        clearCache(context)
    }
}
