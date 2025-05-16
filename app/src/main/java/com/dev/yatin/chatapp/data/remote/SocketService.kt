package com.dev.yatin.chatapp.data.remote

import android.util.Log
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.engineio.client.EngineIOException
import org.json.JSONObject

class SocketService(serverUrl: String) {
    private val socket: Socket = IO.socket(serverUrl)
    private val _messageFlow = MutableSharedFlow<JSONObject>()
    val messageFlow = _messageFlow.asSharedFlow()

    companion object {
        private val TAG = "SocketService"
    }

    init {
        Log.d(TAG, "Socket: init")
        socket.on(Socket.EVENT_CONNECT) {
            // Handle connect
            Log.d(TAG, "Socket: connected")
        }
        socket.on(Socket.EVENT_CONNECT_ERROR) { args ->
            // Handle socket connection error
            val error = if (args.isNotEmpty() && args[0] != null) args[0] else null

            when (error) {
                is EngineIOException -> {
                    Log.e(TAG, "Socket EngineIO error: ${error.message}")
                    // Handle specific EngineIO error, e.g., network issues
                }

                else -> {
                    val errorMessage = error?.toString() ?: "Unknown error"
                    Log.e(TAG, "Socket connect error: $errorMessage")
                    // Handle other types of errors
                }
            }
            // Optionally, inform the user or retry
        }
        socket.on("message") { args ->
            val data = args[0] as JSONObject
            _messageFlow.tryEmit(data)
        }
        socket.connect()
    }

    fun sendMessage(message: JSONObject) {
        socket.emit("message", message)
    }

    fun disconnect() {
        socket.disconnect()
    }
} 