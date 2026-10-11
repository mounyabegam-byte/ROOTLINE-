package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "badges")
data class BadgeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val studentRollNumber: String,
    val title: String,
    val description: String,
    val iconEmoji: String,
    val points: Int,
    val isUnlocked: Boolean = false,
    val unlockedDate: String? = null
)
