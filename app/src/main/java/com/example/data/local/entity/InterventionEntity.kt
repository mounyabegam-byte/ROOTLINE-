package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "interventions")
data class InterventionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val studentRollNumber: String,
    val studentName: String,
    val department: String,
    val type: String, // e.g. "Remedial Classes", "Attendance Plan", "Academic Counseling"
    val issue: String,
    val actionPlan: String,
    val assignedFaculty: String,
    val deadline: String,
    val status: String, // "PROPOSED", "IN_PROGRESS", "COMPLETED", "SUCCESSFUL"
    val beforeRisk: Int,
    val afterRisk: Int,
    val beforeAttendance: Double,
    val afterAttendance: Double,
    val isResolved: Boolean = false,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
