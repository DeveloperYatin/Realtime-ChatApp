package com.dev.yatin.chatapp.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.dev.yatin.chatapp.data.local.entity.MessageEntity

@Dao
interface MessageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity)

    @Update
    suspend fun updateMessage(message: MessageEntity)

    @Query("DELETE FROM messages")
    suspend fun clearAll()

    @Query("SELECT * FROM messages WHERE status = :status")
    suspend fun getMessagesByStatus(status: String): List<MessageEntity>

    @Query("SELECT * FROM messages WHERE chatId = :chatId ORDER BY timestamp ASC")
    fun getMessagesForChat(chatId: String): kotlinx.coroutines.flow.Flow<List<MessageEntity>>

    @Query("SELECT * FROM messages WHERE timestamp IN (SELECT MAX(timestamp) FROM messages GROUP BY chatId) ORDER BY timestamp DESC")
    fun getLatestMessagesForChats(): kotlinx.coroutines.flow.Flow<List<MessageEntity>>
} 