package com.dev.yatin.chatapp.domain.usecase

import com.dev.yatin.chatapp.domain.repository.ChatRepository

class GetChatsUseCase(private val repository: ChatRepository) {
    operator fun invoke() = repository.getChats()
    fun getMessages(chatId: String) = repository.getMessages(chatId)
}