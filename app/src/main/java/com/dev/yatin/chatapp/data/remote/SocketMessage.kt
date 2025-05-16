package com.dev.yatin.chatapp.data.remote

import org.json.JSONObject

sealed class SocketMessage {
    data class Json(val json: JSONObject) : SocketMessage()
    data class Text(val text: String) : SocketMessage()
} 