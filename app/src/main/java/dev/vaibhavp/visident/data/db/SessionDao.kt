package dev.vaibhavp.visident.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import dev.vaibhavp.visident.data.model.SessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: SessionEntity)

    @Update
    suspend fun updateSession(session: SessionEntity)

    @Delete
    suspend fun deleteSession(session: SessionEntity)

    @Query("SELECT * FROM sessions WHERE sessionId = :id LIMIT 1")
    suspend fun getSessionById(id: String): SessionEntity?

    /** Emits the full session list and re-emits whenever the table changes. */
    @Query("SELECT * FROM sessions ORDER BY createdAt DESC")
    fun observeAllSessions(): Flow<List<SessionEntity>>

    /** Case-insensitive search by name or session id, filtered in SQL rather than in memory. */
    @Query(
        "SELECT * FROM sessions " +
            "WHERE name LIKE '%' || :query || '%' OR sessionId LIKE '%' || :query || '%' " +
            "ORDER BY createdAt DESC",
    )
    fun searchSessions(query: String): Flow<List<SessionEntity>>

    // Retained only for the legacy SessionViewModel; removed in Phase 3 once screens use Flows.
    @Query("SELECT * FROM sessions ORDER BY createdAt DESC")
    suspend fun getAllSessions(): List<SessionEntity>
}
