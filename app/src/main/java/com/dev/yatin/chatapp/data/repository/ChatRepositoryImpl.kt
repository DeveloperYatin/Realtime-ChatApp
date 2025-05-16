package com.dev.yatin.chatapp.data.repository

import com.dev.yatin.chatapp.domain.model.Chat
import com.dev.yatin.chatapp.domain.model.Message
import com.dev.yatin.chatapp.domain.model.MessageStatus
import com.dev.yatin.chatapp.domain.repository.ChatRepository
import com.dev.yatin.chatapp.data.remote.SocketService
import com.dev.yatin.chatapp.data.local.MessageDao
import com.dev.yatin.chatapp.data.local.entity.MessageEntity
import com.dev.yatin.chatapp.data.remote.SocketMessage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class ChatRepositoryImpl(
    private val socketService: SocketService,
    private val messageDao: MessageDao
) : ChatRepository {
    private val chatsFlow = MutableStateFlow<List<Chat>>(emptyList())
    private val messagesFlow = MutableStateFlow<Map<String, List<Message>>>(emptyMap())

    val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm:ss", Locale.getDefault())

    init {
        // Fill chatsFlow from local DB
        CoroutineScope(Dispatchers.IO).launch {
            messageDao.getLatestMessagesForChats().collect { entities ->
                val chats = entities.map { entity ->
                    Chat(entity.chatId, entity.text, dateFormat.format(Date(entity.timestamp)))
                }.sortedByDescending { it.timestamp }
                chatsFlow.value = chats
            }
        }
        // Collect socket messages
        CoroutineScope(Dispatchers.IO).launch {
            socketService.messageFlow.collect { socketMsg ->
                when (socketMsg) {
                    is SocketMessage.Json -> {
                        val message = jsonToMessage(socketMsg.json)
                        // Insert received message into the database
                        messageDao.insertMessage(message.toEntity(MessageStatus.SENT))
                        // (Optional) You can keep the in-memory update for chatsFlow if needed
                        messagesFlow.update { map ->
                            val list = map[message.chatId].orEmpty() + message
                            map + (message.chatId to list)
                        }
                        chatsFlow.update { list ->
                            val chat = Chat(message.chatId, message.text, dateFormat.format(Date(message.timestamp)))
                            val filtered = list.filterNot { it.id == chat.id }
                            (filtered + chat).sortedByDescending { it.timestamp }
                        }
                    }
                    is SocketMessage.Text -> {
                        val message = textToMessage(socketMsg.text)
                        messageDao.insertMessage(message.toEntity(MessageStatus.SENT))
                        messagesFlow.update { map ->
                            val list = map[message.chatId].orEmpty() + message
                            map + (message.chatId to list)
                        }
                        chatsFlow.update { list ->
                            val chat = Chat(message.chatId, message.text, dateFormat.format(Date(message.timestamp)))
                            val filtered = list.filterNot { it.id == chat.id }
                            (filtered + chat).sortedByDescending { it.timestamp }
                        }
                        // Log.d("ChatRepositoryImpl", "Received plain text: ${socketMsg.text}")
                    }
                }
            }
        }
    }

    override fun getChats(): Flow<List<Chat>> = chatsFlow.asStateFlow()

    override fun getMessages(chatId: String): Flow<List<Message>> =
        messageDao.getMessagesForChat(chatId).map { list -> list.map { it.toDomain() } }

    override suspend fun sendMessage(message: Message): Boolean {
        return try {
            val json = messageToJson(message)
            socketService.sendMessage(json)
            true
        } catch (_: Exception) {
            // Simulate failure: queue message
            messageDao.insertMessage(message.toEntity(MessageStatus.QUEUED))
            false
        }
    }

    override suspend fun retryQueuedMessages() {
        val queued = messageDao.getMessagesByStatus(MessageStatus.QUEUED.name)
        for (entity in queued) {
            try {
                socketService.sendMessage(messageToJson(entity.toDomain()))
                messageDao.updateMessage(entity.copy(status = MessageStatus.SENT.name))
            } catch (_: Exception) {
                // Still failed, keep queued
            }
        }
    }

    // Helper mapping functions
    private fun messageToJson(message: Message): JSONObject = JSONObject().apply {
        put("id", message.id)
        put("chatId", message.chatId)
        put("text", message.text)
        put("sender", message.sender)
        put("timestamp", message.timestamp)
    }

    private fun jsonToMessage(json: JSONObject): Message = Message(
        id = json.getString("id"),
        chatId = json.getString("chatId"),
        text = json.getString("text"),
        sender = json.getString("sender"),
        timestamp = Date().time,
        status = MessageStatus.SENT
    )

    private fun textToMessage(text: String): Message = Message(
        id = UUID.randomUUID().toString(),
        chatId = UUID.randomUUID().toString(),
        text = text,
        sender = "Server", // can change with sender name when needed
        timestamp = Date().time,
        status = MessageStatus.SENT
    )

    private fun Message.toEntity(status: MessageStatus) = MessageEntity(
        id = id,
        chatId = chatId,
        text = text,
        sender = sender,
        timestamp = Date().time,
        status = status.name
    )

    private fun MessageEntity.toDomain() = Message(
        id = id,
        chatId = chatId,
        text = text,
        sender = sender,
        timestamp = timestamp,
        status = MessageStatus.valueOf(status)
    )
} 