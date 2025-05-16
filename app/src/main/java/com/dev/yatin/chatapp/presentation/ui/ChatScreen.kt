package com.dev.yatin.chatapp.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.dev.yatin.chatapp.presentation.viewmodel.ChatViewModel
import kotlinx.coroutines.launch

@Composable
fun ChatScreen(viewModel: ChatViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedChatId by remember { mutableStateOf<String?>(null) }
    var messageText by remember { mutableStateOf(TextFieldValue()) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Show error Snackbar
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { msg ->
            scope.launch {
                snackbarHostState.showSnackbar(msg)
                viewModel.clearError()
            }
        }
    }

    Column(Modifier.fillMaxSize()) {
        // Offline banner
        if (!uiState.isConnected) {
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
        Row(Modifier.weight(1f)) {
            // Chat List
            if (uiState.emptyChats) {
                Box(
                    Modifier.weight(1f).fillMaxHeight(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send, //Icons.Default.Chat
                            contentDescription = "No chats",
                            tint = Color.Gray,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(Modifier.height(8.dp))
                        Text("No chats available", color = Color.Gray)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxHeight()
                ) {
                    items(uiState.chats) { chat ->
                        ListItem(
                            headlineContent = { Text(chat.lastMessage) },
                            supportingContent = { Text("${chat.timestamp}") },
                            modifier = Modifier.clickable {
                                selectedChatId = chat.id
                                viewModel.loadMessages(chat.id)
                            }
                        )
                        HorizontalDivider()
                    }
                }
            }
            // Chat Messages & Input
            selectedChatId?.let { chatId ->
                Column(
                    modifier = Modifier.weight(2f).fillMaxHeight().padding(8.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    val messages = uiState.messages
                    if (messages.isEmpty()) {
                        Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Send, //Icons.Default.Message
                                    contentDescription = "No messages",
                                    tint = Color.Gray,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(Modifier.height(8.dp))
                                Text("No messages yet", color = Color.Gray)
                            }
                        }
                    } else {
                        LazyColumn(modifier = Modifier.weight(1f)) {
                            items(messages) { msg ->
                                Text("${msg.sender}: ${msg.text}")
                            }
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TextField(
                            value = messageText,
                            onValueChange = { messageText = it },
                            modifier = Modifier.weight(1f),
                            placeholder = { Text("Type a message...") }
                        )
                        Button(onClick = {
                            viewModel.sendMessage(messageText.text, "You")
                            messageText = TextFieldValue()
                        }) {
                            Text("Send")
                        }
                    }
                }
            }
        }
        SnackbarHost(hostState = snackbarHostState)
    }
} 