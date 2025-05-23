package com.dev.yatin.chatapp.data.remote

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okhttp3.Response
import okio.ByteString
import org.json.JSONObject

/*
Expected server side response :- {"id":"1","chatId":"1","text":"Message","sender":"Server"}
*/
class SocketService(serverUrl: String) {
    private val client = OkHttpClient()
    private var webSocket: WebSocket? = null
    private val _messageFlow = MutableSharedFlow<SocketMessage>()
    val messageFlow = _messageFlow.asSharedFlow()

    companion object {
        private val TAG = "SocketService"
    }

    init {
        Log.d(TAG, "WebSocket: init")
        val request = Request.Builder().url(serverUrl).build()
        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d(TAG, "WebSocket: connected")
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val json = JSONObject(text)
                        Log.d(TAG, "WebSocket: Emitting JSON message: $text")
                        _messageFlow.emit(SocketMessage.Json(json))
                    } catch (e: Exception) {
                        Log.w(TAG, "WebSocket: Emitting plain text message: $text")
                        _messageFlow.emit(SocketMessage.Text(text))
                    }
                }
            }

            override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
                // Handle binary messages if needed
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                Log.d(TAG, "WebSocket: closing: $code / $reason")
                webSocket.close(code, reason)
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                Log.d(TAG, "WebSocket: closed: $code / $reason")
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e(TAG, "WebSocket: error", t)
            }
        })
    }

    fun sendMessage(message: JSONObject) {
        webSocket?.send(message.toString())
    }

    fun disconnect() {
        webSocket?.close(1000, "Client disconnect")
    }
} 