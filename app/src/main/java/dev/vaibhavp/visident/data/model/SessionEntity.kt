package dev.vaibhavp.visident.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sessions")
data class SessionEntity(
    @PrimaryKey val sessionId: String,
    val name: String,
    val age: Int,
    val createdAt: Long = System.currentTimeMillis(),
    val imageCount: Int = 0
)