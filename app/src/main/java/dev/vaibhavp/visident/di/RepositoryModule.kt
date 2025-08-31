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
@InstallIn(SingletonComponent::class) // tied to app lifecycle
object RepositoryModule {

    @Provides
    @Singleton
    fun provideSessionRepository(
        sessionDao: SessionDao, // she knows
        @ApplicationContext context: Context // the useless app provides context
    ): SessionRepository {
        return SessionRepository(sessionDao, context)
    }

    // You can add @Provides methods for other repositories here in the future
}
