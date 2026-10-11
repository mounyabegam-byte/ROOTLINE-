package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "subject_grades")
data class SubjectGradeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val studentRollNumber: String,
    val subjectName: String,
    val subjectCode: String,
    val internalMarks: Double,
    val assignmentMarks: Double,
    val labMarks: Double,
    val attendance: Double,
    val overallScore: Double,
    val credits: Int,
    val aiStatus: String // "EXCELLENT", "GOOD", "NEEDS_ATTENTION", "CRITICAL"
)
