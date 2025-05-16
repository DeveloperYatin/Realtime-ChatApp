package com.dev.yatin.chatapp.di

import android.content.Context
import androidx.room.Room
import com.dev.yatin.chatapp.data.local.AppDatabase
import com.dev.yatin.chatapp.data.remote.SocketService
import com.dev.yatin.chatapp.data.repository.ChatRepositoryImpl
import com.dev.yatin.chatapp.domain.repository.ChatRepository
import com.dev.yatin.chatapp.domain.usecase.GetChatsUseCase
import com.dev.yatin.chatapp.domain.usecase.SendMessageUseCase
import com.dev.yatin.chatapp.domain.usecase.RetryQueuedMessagesUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    fun provideContext(@ApplicationContext context: Context): Context {
        return context
    }

    @Provides
    @Singleton
    fun provideSocketService(): SocketService = SocketService("socket_url")

    @Provides
    @Singleton
    fun provideDatabase(appContext: Context): AppDatabase =
        Room.databaseBuilder(appContext, AppDatabase::class.java, "chat-db").build()

    @Provides
    fun provideMessageDao(db: AppDatabase) = db.messageDao()

    @Provides
    @Singleton
    fun provideChatRepository(
        socketService: SocketService,
        messageDao: com.dev.yatin.chatapp.data.local.MessageDao
    ): ChatRepository = ChatRepositoryImpl(socketService, messageDao)

    @Provides
    fun provideGetChatsUseCase(repository: ChatRepository) = GetChatsUseCase(repository)

    @Provides
    fun provideSendMessageUseCase(repository: ChatRepository) = SendMessageUseCase(repository)

    @Provides
    fun provideRetryQueuedMessagesUseCase(repository: ChatRepository) = RetryQueuedMessagesUseCase(repository)
} 