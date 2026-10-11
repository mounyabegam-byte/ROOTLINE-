package com.example.model

enum class UserRole(val label: String, val iconEmoji: String) {
    STUDENT("Student", "👨‍🎓"),
    STAFF("Staff / Faculty", "👨‍🏫"),
    FACULTY("Faculty", "👨‍🏫"),
    HOD("HOD / Dept Head", "🏢"),
    PRINCIPAL("Principal", "🎓"),
    ADMIN("Super Admin", "👑"),
    MENTOR("Mentor", "🤝")
}

enum class AcademicRiskLevel(val label: String, val colorHex: Long) {
    LOW("Low Risk", 0xFF10B981),
    MEDIUM("Medium Risk", 0xFFF59E0B),
    HIGH("High Risk", 0xFFEF4444)
}

enum class StudentClusterType(val title: String, val description: String) {
    HIGH_PERFORMERS("High Performers", "Consistent high marks, attendance > 85%, active engagement"),
    CONSISTENT_PERFORMERS("Consistent Performers", "Steady academic metrics, good attendance, minimal intervention"),
    IMPROVEMENT_REQUIRED("Improvement Required", "Moderate grades, attendance slipping 70-75%, needs tracking"),
    AT_RISK("At Risk (Critical)", "Attendance < 70%, backlogs, declining marks, immediate intervention needed")
}

data class RiskFactorContribution(
    val factorName: String,
    val currentValue: String,
    val impactWeight: Float, // 0.0 to 1.0
    val isNegative: Boolean,
    val statusText: String
)

data class TopicPerformance(
    val topicName: String,
    val subjectName: String,
    val averageScore: Int,
    val isWeakGap: Boolean,
    val recommendation: String
)

data class DepartmentStats(
    val department: String,
    val totalStudents: Int,
    val averageGpa: Double,
    val averageAttendance: Double,
    val passPercentage: Double,
    val highRiskCount: Int,
    val backlogsCount: Int
)

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val text: String,
    val isFromUser: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val quickActions: List<String> = emptyList()
)

data class AttendanceRecord(
    val id: String = java.util.UUID.randomUUID().toString(),
    val studentRollNumber: String,
    val subjectCode: String,
    val subjectName: String,
    val totalClasses: Int,
    val attendedClasses: Int,
    val percentage: Double = if (totalClasses > 0) (attendedClasses.toDouble() / totalClasses * 100.0) else 0.0,
    val lastUpdated: Long = System.currentTimeMillis()
)

