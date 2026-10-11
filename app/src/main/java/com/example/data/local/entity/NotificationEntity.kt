package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val studentRollNumber: String,
    val title: String,
    val message: String,
    val type: String, // "CRITICAL", "WARNING", "INFO", "SUCCESS"
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)
