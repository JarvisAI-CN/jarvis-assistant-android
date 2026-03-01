package com.assistant.voip.di

import android.content.Context
import com.assistant.voip.data.database.AppDatabase
import com.assistant.voip.data.database.dao.CallDao
import com.assistant.voip.data.database.dao.FileTransferDao
import com.assistant.voip.data.database.dao.TaskDao
import com.assistant.voip.data.repository.CallRepositoryImpl
import com.assistant.voip.data.repository.FileTransferRepositoryImpl
import com.assistant.voip.data.repository.TaskRepositoryImpl
import com.assistant.voip.data.repository.impl.BaiduSpeechRepository
import com.assistant.voip.domain.repository.CallRepository
import com.assistant.voip.domain.repository.FileTransferRepository
import com.assistant.voip.domain.repository.SpeechRepository
import com.assistant.voip.domain.repository.TaskRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return AppDatabase.getDatabase(context)
    }

    @Provides
    @Singleton
    fun provideCallDao(database: AppDatabase): CallDao {
        return database.callDao()
    }

    @Provides
    @Singleton
    fun provideTaskDao(database: AppDatabase): TaskDao {
        return database.taskDao()
    }

    @Provides
    @Singleton
    fun provideFileTransferDao(database: AppDatabase): FileTransferDao {
        return database.fileTransferDao()
    }
}

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideCallRepository(
        callDao: CallDao
    ): CallRepository {
        return CallRepositoryImpl(callDao)
    }

    @Provides
    @Singleton
    fun provideTaskRepository(
        taskDao: TaskDao
    ): TaskRepository {
        return TaskRepositoryImpl(taskDao)
    }

    @Provides
    @Singleton
    fun provideFileTransferRepository(
        fileTransferDao: FileTransferDao
    ): FileTransferRepository {
        return FileTransferRepositoryImpl(fileTransferDao)
    }

    @Provides
    @Singleton
    fun provideSpeechRepository(
        @ApplicationContext context: Context
    ): SpeechRepository {
        return BaiduSpeechRepository(context)
    }
}

@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {

    @Provides
    @Singleton
    fun provideCheckPermissionsUseCase(): com.assistant.voip.domain.usecase.CheckPermissionsUseCase {
        return com.assistant.voip.domain.usecase.CheckPermissionsUseCase()
    }

    @Provides
    @Singleton
    fun provideRequestPermissionsUseCase(): com.assistant.voip.domain.usecase.RequestPermissionsUseCase {
        return com.assistant.voip.domain.usecase.RequestPermissionsUseCase()
    }

    @Provides
    @Singleton
    fun provideRecognizeSpeechUseCase(
        speechRepository: SpeechRepository
    ): com.assistant.voip.domain.usecase.RecognizeSpeechUseCase {
        return com.assistant.voip.domain.usecase.RecognizeSpeechUseCase(speechRepository)
    }

    @Provides
    @Singleton
    fun provideSynthesizeSpeechUseCase(
        speechRepository: SpeechRepository
    ): com.assistant.voip.domain.usecase.SynthesizeSpeechUseCase {
        return com.assistant.voip.domain.usecase.SynthesizeSpeechUseCase(speechRepository)
    }

    @Provides
    @Singleton
    fun provideStopSpeechRecognitionUseCase(
        speechRepository: SpeechRepository
    ): com.assistant.voip.domain.usecase.StopSpeechRecognitionUseCase {
        return com.assistant.voip.domain.usecase.StopSpeechRecognitionUseCase(speechRepository)
    }
}
