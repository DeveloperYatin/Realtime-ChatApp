package com.dev.yatin.chatapp.domain.usecase

import com.dev.yatin.chatapp.domain.model.Message
import com.dev.yatin.chatapp.domain.repository.ChatRepository

class SendMessageUseCase(private val repository: ChatRepository) {
    suspend operator fun invoke(message: Message): Boolean = repository.sendMessage(message)
} 