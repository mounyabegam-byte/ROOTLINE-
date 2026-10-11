package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "students")
data class StudentEntity(
    @PrimaryKey
    val rollNumber: String,
    val name: String,
    val department: String,
    val year: Int,
    val semester: Int,
    val section: String,
    val email: String,
    val phone: String,
    val attendance: Double,
    val internalMarks: Double,
    val assignmentScore: Double,
    val labScore: Double,
    val quizScore: Double,
    val previousGpa: Double,
    val currentGpa: Double,
    val predictedGpa: Double,
    val backlogs: Int,
    val engagementScore: Double,
    val riskScore: Int, // 0 - 100
    val riskLevel: String, // "LOW", "MEDIUM", "HIGH"
    val successScore: Int, // 0 - 100
    val cluster: String,
    val advisorName: String,
    val lastUpdated: Long = System.currentTimeMillis()
)
