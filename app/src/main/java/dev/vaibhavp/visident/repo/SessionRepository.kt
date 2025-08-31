package dev.vaibhavp.visident.repo

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.vaibhavp.visident.data.db.SessionDao
import dev.vaibhavp.visident.data.model.SessionEntity
import dev.vaibhavp.visident.util.FileUtils
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionRepository @Inject constructor(
    private val dao: SessionDao,
    @field:ApplicationContext private val context: Context
) {

    suspend fun saveSession(session: SessionEntity) {
        dao.insertSession(session)
        FileUtils.createSessionFolder(context, session.sessionId)
    }

    suspend fun getSession(id: String): SessionEntity? {
        return dao.getSessionById(id)
    }

    suspend fun getAllSessions(): List<SessionEntity> {
        return dao.getAllSessions()
    }

    fun getImagesForSession(sessionId: String): List<File> {
        return FileUtils.getSessionImages(context, sessionId)
    }

    fun moveCachedImagesToSession(sessionId: String) {
        FileUtils.moveCachedImagesToSession(context, sessionId)
    }

    fun createTempImageFile(): File {
        return FileUtils.createTempImageFile(context)
    }

    fun clearCache() {
        FileUtils.clearCache(context)
    }
}
