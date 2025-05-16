package com.dev.yatin.chatapp.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.dev.yatin.chatapp.data.local.entity.MessageEntity

@Database(entities = [MessageEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun messageDao(): MessageDao
} 