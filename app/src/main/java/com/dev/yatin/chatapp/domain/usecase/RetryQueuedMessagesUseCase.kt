package com.dev.yatin.chatapp.domain.usecase

import com.dev.yatin.chatapp.domain.repository.ChatRepository

class RetryQueuedMessagesUseCase(private val repository: ChatRepository) {
    suspend operator fun invoke() = repository.retryQueuedMessages()
}