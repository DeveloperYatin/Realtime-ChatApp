package com.dev.yatin.chatapp.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dev.yatin.chatapp.domain.model.Chat
import com.dev.yatin.chatapp.domain.model.Message
import com.dev.yatin.chatapp.domain.model.MessageStatus
import com.dev.yatin.chatapp.domain.usecase.GetChatsUseCase
import com.dev.yatin.chatapp.domain.usecase.SendMessageUseCase
import com.dev.yatin.chatapp.domain.usecase.RetryQueuedMessagesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

// UI State data class
data class ChatUiState(
    val chats: List<Chat> = emptyList(),
    val messages: List<Message> = emptyList(),
    val errorMessage: String? = null,
    val isConnected: Boolean = true,
    val emptyChats: Boolean = true
)

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val getChatsUseCase: GetChatsUseCase,
    private val sendMessageUseCase: SendMessageUseCase,
    private val retryQueuedMessagesUseCase: RetryQueuedMessagesUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private var currentChatId: String? = null

    init {
        viewModelScope.launch {
            getChatsUseCase().collectLatest { chats ->
                _uiState.value = _uiState.value.copy(
                    chats = chats,
                    emptyChats = chats.isEmpty(),
                )
            }
        }
    }

    fun loadMessages(chat: Chat) {
        currentChatId = chat.id
        // In a real app, use a use case for getMessages(chatId)
        viewModelScope.launch {
            getChatsUseCase.getMessages(chat.id).collectLatest {
                _uiState.value = _uiState.value.copy(messages = it)
            }
        }
    }

    fun sendMessage(text: String, sender: String) {
        val chatId = currentChatId ?: UUID.randomUUID().toString()
        val message = Message(
            id = UUID.randomUUID().toString(),
            chatId = chatId,
            text = text,
            sender = sender,
            timestamp = System.currentTimeMillis(),
            status = MessageStatus.SENT
        )
        _uiState.value = _uiState.value.copy(messages = _uiState.value.messages + message)
        viewModelScope.launch {
            val success = try {
                sendMessageUseCase(message)
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = "Failed to send message: ${e.localizedMessage}")
                false
            }
            if (!success) {
                _uiState.value = _uiState.value.copy(errorMessage = "Message queued due to network error.")
            }
        }
    }

    fun retryQueuedMessages() {
        viewModelScope.launch {
            try {
                retryQueuedMessagesUseCase()
            } catch (_: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = "Failed to retry queued messages.")
            }
        }
    }

    fun setConnectivity(isConnected: Boolean) {
        val wasDisconnected = !_uiState.value.isConnected
        _uiState.value = _uiState.value.copy(isConnected = isConnected)
        if (isConnected && wasDisconnected) {
            retryQueuedMessages()
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
} 