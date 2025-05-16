package com.dev.yatin.chatapp.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.dev.yatin.chatapp.presentation.viewmodel.ChatViewModel
import kotlinx.coroutines.launch
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import com.dev.yatin.chatapp.domain.model.Chat
import com.dev.yatin.chatapp.domain.model.Message

@Composable
fun ChatScreen(viewModel: ChatViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedChatId by remember { mutableStateOf<String?>(null) }
    var messageText by remember { mutableStateOf(TextFieldValue()) }
    val uiSnackBarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Show error SnackBar
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { msg ->
            scope.launch {
                uiSnackBarHostState.showSnackbar(msg)
                viewModel.clearError()
            }
        }
    }

    Column(Modifier.fillMaxSize()) {
        OfflineBanner(isConnected = uiState.isConnected)
        Row(Modifier.weight(1f)) {
            if (uiState.emptyChats) {
                EmptyChatsView(Modifier.weight(1f).fillMaxHeight())
            } else {
                ChatList(
                    chats = uiState.chats,
                    onChatSelected = { chat ->
                        selectedChatId = chat.id
                        viewModel.loadMessages(chat)
                    },
                    modifier = Modifier.weight(1f).fillMaxHeight()
                )
            }
            selectedChatId?.let { chatId ->
                ChatMessagesPanel(
                    messages = uiState.messages,
                    onSend = { text ->
                        viewModel.sendMessage(text, "You")
                        messageText = TextFieldValue()
                    },
                    messageText = messageText,
                    onMessageTextChange = { messageText = it },
                    modifier = Modifier.weight(2f).fillMaxHeight().padding(8.dp)
                )
            }
        }
        SnackbarHost(hostState = uiSnackBarHostState)
    }
}

@Composable
fun OfflineBanner(isConnected: Boolean) {
    if (!isConnected) {
        Box(
            Modifier
                .fillMaxWidth()
                .background(Color.Red)
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("No internet connection", color = Color.White)
        }
    }
}

@Composable
fun EmptyChatsView(modifier: Modifier = Modifier) {
    Box(
        modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Send,
                contentDescription = "No chats",
                tint = Color.Gray,
                modifier = Modifier.size(64.dp)
            )
            Spacer(Modifier.height(8.dp))
            Text("No chats available", color = Color.Gray)
        }
    }
}

@Composable
fun ChatList(
    chats: List<Chat>,
    onChatSelected: (Chat) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(modifier = modifier) {
        items(chats, key = { it.id }) { chat ->
            ChatListItem(chat = chat, onClick = { onChatSelected(chat) })
            HorizontalDivider()
        }
    }
}

@Composable
fun ChatListItem(chat: Chat, onClick: () -> Unit) {
    ListItem(
        leadingContent = {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "User Icon",
                tint = Color.Gray,
                modifier = Modifier.size(40.dp).clip(CircleShape)
            )
        },
        headlineContent = { Text(chat.lastMessage, maxLines = 1) },
        supportingContent = { Text(chat.timestamp) },
        modifier = Modifier.clickable { onClick() }
    )
}

@Composable
fun ChatMessagesPanel(
    messages: List<Message>,
    onSend: (String) -> Unit,
    messageText: TextFieldValue,
    onMessageTextChange: (TextFieldValue) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        if (messages.isEmpty()) {
            EmptyMessagesView(Modifier.weight(1f))
        } else {
            MessagesList(messages = messages, modifier = Modifier.weight(1f))
        }
        MessageInputBar(
            messageText = messageText,
            onMessageTextChange = onMessageTextChange,
            onSend = onSend
        )
    }
}

@Composable
fun EmptyMessagesView(modifier: Modifier = Modifier) {
    Box(modifier, contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Send,
                contentDescription = "No messages",
                tint = Color.Gray,
                modifier = Modifier.size(48.dp)
            )
            Spacer(Modifier.height(8.dp))
            Text("No messages yet", color = Color.Gray)
        }
    }
}

@Composable
fun MessagesList(messages: List<Message>, modifier: Modifier = Modifier) {
    LazyColumn(modifier = modifier) {
        items(messages, key = { it.id }) { msg ->
            MessageBubble(msg)
            Spacer(Modifier.height(4.dp))
        }
    }
}

@Composable
fun MessageBubble(msg: Message) {
    val isServer = msg.sender.equals("Server", ignoreCase = true)
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isServer) Arrangement.End else Arrangement.Start
    ) {
        if (!isServer) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "User Icon",
                tint = Color(0xFF90CAF9),
                modifier = Modifier.size(32.dp).clip(CircleShape).align(Alignment.Bottom)
            )
            Spacer(Modifier.width(4.dp))
        }
        Column(
            horizontalAlignment = if (isServer) Alignment.End else Alignment.Start
        ) {
            Text(
                text = msg.sender,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Gray,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            Surface(
                color = if (isServer) Color(0xFFDCF8C6) else Color(0xFFFFFFFF),
                shape = RoundedCornerShape(12.dp),
                tonalElevation = 2.dp,
                modifier = Modifier.padding(2.dp)
            ) {
                Text(
                    text = msg.text,
                    modifier = Modifier.padding(12.dp),
                    textAlign = if (isServer) TextAlign.End else TextAlign.Start
                )
            }
        }
        if (isServer) {
            Spacer(Modifier.width(4.dp))
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "User Icon",
                tint = Color(0xFF81C784),
                modifier = Modifier.size(32.dp).clip(CircleShape).align(Alignment.Bottom)
            )
        }
    }
}

@Composable
fun MessageInputBar(
    messageText: TextFieldValue,
    onMessageTextChange: (TextFieldValue) -> Unit,
    onSend: (String) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .padding(8.dp)
            .background(Color(0xFFF5F5F5), RoundedCornerShape(24.dp))
            .fillMaxWidth()
            .height(56.dp)
    ) {
        TextField(
            value = messageText,
            onValueChange = onMessageTextChange,
            modifier = Modifier.weight(1f).background(Color.Transparent),
            placeholder = { Text("Type a message...") },
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = Color.Transparent,
                focusedContainerColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent
            ),
            maxLines = 2,
            singleLine = false
        )
        IconButton(
            onClick = {
                if (messageText.text.isNotBlank()) onSend(messageText.text)
            },
            modifier = Modifier.padding(end = 8.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Send,
                contentDescription = "Send",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
} 