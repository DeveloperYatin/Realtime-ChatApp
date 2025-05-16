package com.dev.yatin.chatapp.domain.repository

import com.dev.yatin.chatapp.domain.model.Chat
import com.dev.yatin.chatapp.domain.model.Message
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    fun getChats(): Flow<List<Chat>>
    fun getMessages(chatId: String): Flow<List<Message>>
    suspend fun sendMessage(message: Message): Boolean
    suspend fun retryQueuedMessages()
} 