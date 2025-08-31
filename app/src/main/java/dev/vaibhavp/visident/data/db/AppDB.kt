package dev.vaibhavp.visident.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import dev.vaibhavp.visident.data.model.SessionEntity

@Database(
    entities = [SessionEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDB : RoomDatabase() {
    abstract fun sessionDao(): SessionDao
}