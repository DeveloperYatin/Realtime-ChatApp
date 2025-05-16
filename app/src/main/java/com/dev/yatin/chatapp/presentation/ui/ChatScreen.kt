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

    Column(Modifier.fillMaxSize().padding(top = 38.dp, bottom = 38.dp)) {
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
                            leadingContent = {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "User Icon",
                                    tint = Color.Gray,
                                    modifier = Modifier.size(40.dp).clip(CircleShape)
                                )
                            },
                            headlineContent = { Text(chat.lastMessage, maxLines = 1) },
                            supportingContent = { Text("${chat.timestamp}") },
                            modifier = Modifier.clickable {
                                selectedChatId = chat.id
                                viewModel.loadMessages(chat)
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
                                val isServer = msg.sender.equals("server", ignoreCase = true)
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
                                Spacer(Modifier.height(4.dp))
                            }
                        }
                    }
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
                            onValueChange = { messageText = it },
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
                                viewModel.sendMessage(messageText.text, "You")
                                messageText = TextFieldValue()
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
            }
        }
        SnackbarHost(hostState = uiSnackBarHostState)
    }
} 