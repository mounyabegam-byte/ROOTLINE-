package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "study_tasks")
data class StudyTaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val studentRollNumber: String,
    val dayOfWeek: String, // "Monday", "Tuesday", etc.
    val orderIndex: Int,
    val subject: String,
    val taskDescription: String,
    val durationMinutes: Int,
    val priority: String, // "HIGH", "MEDIUM", "LOW"
    val isCompleted: Boolean = false
)
