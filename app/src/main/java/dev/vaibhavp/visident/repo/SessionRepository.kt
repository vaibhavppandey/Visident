package dev.vaibhavp.visident.repo

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.vaibhavp.visident.data.db.SessionDao
import dev.vaibhavp.visident.data.model.SessionEntity
import dev.vaibhavp.visident.util.FileUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/** Single point of access to session metadata (Room) and session images (file storage). */
@Singleton
class SessionRepository @Inject constructor(
    private val dao: SessionDao,
    @param:ApplicationContext private val context: Context,
) {

    suspend fun saveSession(session: SessionEntity) {
        dao.insertSession(session)
        withContext(Dispatchers.IO) { FileUtils.createSessionFolder(context, session.sessionId) }
    }

    suspend fun updateSession(session: SessionEntity) = dao.updateSession(session)

    /** Deletes the session row and its image folder together. */
    suspend fun deleteSession(session: SessionEntity) {
        dao.deleteSession(session)
        withContext(Dispatchers.IO) { FileUtils.deleteSessionFolder(context, session.sessionId) }
    }

    suspend fun getSession(id: String): SessionEntity? = dao.getSessionById(id)

    fun observeAllSessions(): Flow<List<SessionEntity>> = dao.observeAllSessions()

    fun searchSessions(query: String): Flow<List<SessionEntity>> = dao.searchSessions(query)

    suspend fun getImagesForSession(sessionId: String): List<File> =
        withContext(Dispatchers.IO) { FileUtils.getSessionImages(context, sessionId) }

    suspend fun moveCachedImagesToSession(sessionId: String) =
        withContext(Dispatchers.IO) { FileUtils.moveCachedImagesToSession(context, sessionId) }

    suspend fun createTempImageFile(): File =
        withContext(Dispatchers.IO) { FileUtils.createTempImageFile(context) }

    suspend fun clearCache() = withContext(Dispatchers.IO) { FileUtils.clearCache(context) }

    // Retained for the legacy SessionViewModel; removed in Phase 3.
    suspend fun getAllSessions(): List<SessionEntity> = dao.getAllSessions()
}
