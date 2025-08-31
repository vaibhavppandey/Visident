package dev.vaibhavp.visident.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import dev.vaibhavp.visident.data.model.SessionEntity

@Dao
interface SessionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: SessionEntity)

    @Query("SELECT * FROM sessions WHERE sessionId = :id LIMIT 1")
    suspend fun getSessionById(id: String): SessionEntity?

    @Query("SELECT * FROM sessions ORDER BY createdAt DESC")
    suspend fun getAllSessions(): List<SessionEntity>
}