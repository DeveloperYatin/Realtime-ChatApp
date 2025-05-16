package com.dev.yatin.chatapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "messages",primaryKeys = [ "id","chatId"])
data class MessageEntity(
    val id: String,
    val chatId: String,
    val text: String,
    val sender: String,
    val timestamp: Long,
    val status: String
) 