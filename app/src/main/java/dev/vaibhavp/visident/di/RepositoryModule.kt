package dev.vaibhavp.visident.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dev.vaibhavp.visident.data.db.SessionDao
import dev.vaibhavp.visident.repo.SessionRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideSessionRepository(
        sessionDao: SessionDao,
        @ApplicationContext context: Context,
    ): SessionRepository = SessionRepository(sessionDao, context)
}
