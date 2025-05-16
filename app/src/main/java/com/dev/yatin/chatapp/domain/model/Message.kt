package com.dev.yatin.chatapp.domain.model

enum class MessageStatus {
    SENT, QUEUED, FAILED
}

data class Message(
    val id: String,
    val chatId: String,
    val text: String,
    val sender: String,
    val timestamp: Long,
    val status: MessageStatus
) 