package dev.vaibhavp.visident.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dev.vaibhavp.visident.data.db.AppDB
import dev.vaibhavp.visident.data.db.SessionDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton // only 1
    fun provideAppDatabase(@ApplicationContext appContext: Context): AppDB {
        return Room.databaseBuilder(
            appContext,
            AppDB::class.java,
            "visident_db" // Ensure this name is consistent
        ).build()
    }

    @Provides
    @Singleton
    fun provideSessionDao(appDB: AppDB): SessionDao {
        return appDB.sessionDao()
    }
}
