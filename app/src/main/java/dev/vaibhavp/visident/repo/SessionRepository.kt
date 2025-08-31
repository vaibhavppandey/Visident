package dev.vaibhavp.visident.repo

import dev.vaibhavp.visident.data.db.SessionDao
import dev.vaibhavp.visident.data.model.SessionEntity

class SessionRepository(private val dao: SessionDao) {

    suspend fun saveSession(session: SessionEntity) {
        dao.insertSession(session)
    }

    suspend fun getSession(id: String): SessionEntity? {
        return dao.getSessionById(id)
    }

    suspend fun getAllSessions(): List<SessionEntity> {
        return dao.getAllSessions()
    }
}
