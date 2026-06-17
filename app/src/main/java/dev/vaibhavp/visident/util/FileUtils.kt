package dev.vaibhavp.visident.util

import android.content.Context
import timber.log.Timber
import java.io.File
import java.io.IOException

/**
 * Helpers for the app's session image storage, rooted at the app-specific external media
 * directory (no storage permission required; removed on uninstall).
 */
object FileUtils {

    private fun getAppFolder(context: Context): File {
        val appSpecificMediaDir = context.externalMediaDirs.firstOrNull()
            ?: throw IOException("External media storage is not available.")
        val sessionsDir = File(appSpecificMediaDir, "Sessions")
        if (!sessionsDir.exists()) {
            sessionsDir.mkdirs()
        }
        return sessionsDir
    }

    fun createSessionFolder(context: Context, sessionId: String): File {
        val sessionFolder = File(getAppFolder(context), sessionId)
        if (!sessionFolder.exists()) {
            sessionFolder.mkdirs()
        }
        return sessionFolder
    }

    fun getSessionImages(context: Context, sessionId: String): List<File> {
        val folder = createSessionFolder(context, sessionId)
        // Return image files only, sorted by capture time (filename carries the timestamp).
        return folder.listFiles { file -> file.isImage() }
            ?.sortedBy { it.name }
            ?: emptyList()
    }

    /** Removes a session's image folder and all its contents. Used when deleting a session. */
    fun deleteSessionFolder(context: Context, sessionId: String) {
        val folder = File(getAppFolder(context), sessionId)
        if (folder.exists() && !folder.deleteRecursively()) {
            Timber.w("Failed to fully delete session folder: %s", sessionId)
        }
    }

    private fun getCacheFolder(context: Context): File {
        val cacheFolder = File(context.cacheDir, "temp")
        if (!cacheFolder.exists()) cacheFolder.mkdirs()
        return cacheFolder
    }

    fun createTempImageFile(context: Context): File {
        val cacheFolder = getCacheFolder(context)
        return File(cacheFolder, "IMG_${System.currentTimeMillis()}.jpg")
    }

    fun getCachedImages(context: Context): List<File> {
        return getCacheFolder(context).listFiles { file -> file.isImage() }?.toList() ?: emptyList()
    }

    fun clearCache(context: Context) {
        getCacheFolder(context).listFiles()?.forEach { file ->
            if (file.isFile) file.delete()
        }
    }

    fun moveCachedImagesToSession(context: Context, sessionId: String) {
        val cachedImages = getCachedImages(context)
        if (cachedImages.isEmpty()) return

        val sessionFolder = createSessionFolder(context, sessionId)
        cachedImages.forEach { file ->
            val targetFile = File(sessionFolder, file.name)
            try {
                // Prefer an atomic rename; fall back to copy+delete across filesystem boundaries.
                if (file.renameTo(targetFile)) return@forEach
                file.copyTo(targetFile, overwrite = true)
                // Delete the source only after a verified copy, so a failed move keeps the original.
                file.delete()
            } catch (e: Exception) {
                // Leave the source in the cache on failure rather than destroying an unsaved photo.
                Timber.e(e, "Failed to move cached image %s", file.name)
            }
        }
    }

    private fun File.isImage(): Boolean =
        isFile && (name.endsWith(".jpg", true) ||
            name.endsWith(".jpeg", true) ||
            name.endsWith(".png", true))
}
