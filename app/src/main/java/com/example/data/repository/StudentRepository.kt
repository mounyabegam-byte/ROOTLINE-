package com.example.data.repository

import com.example.data.ai.AiAnalyticsEngine
import com.example.data.local.AppDatabase
import com.example.data.local.SampleData
import com.example.data.local.entity.BadgeEntity
import com.example.data.local.entity.InterventionEntity
import com.example.data.local.entity.NotificationEntity
import com.example.data.local.entity.StudentEntity
import com.example.data.local.entity.StudyTaskEntity
import com.example.data.local.entity.SubjectGradeEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class StudentRepository(private val database: AppDatabase) {

    private val studentDao = database.studentDao()
    private val subjectGradeDao = database.subjectGradeDao()
    private val interventionDao = database.interventionDao()
    private val notificationDao = database.notificationDao()
    private val studyTaskDao = database.studyTaskDao()
    private val badgeDao = database.badgeDao()

    val allStudents: Flow<List<StudentEntity>> = studentDao.getAllStudents()
    val highRiskStudents: Flow<List<StudentEntity>> = studentDao.getHighRiskStudents()
    val allInterventions: Flow<List<InterventionEntity>> = interventionDao.getAllInterventions()
    val allNotifications: Flow<List<NotificationEntity>> = notificationDao.getAllNotifications()

    fun getStudent(rollNumber: String): Flow<StudentEntity?> =
        studentDao.getStudentByRoll(rollNumber)

    fun getGradesForStudent(rollNumber: String): Flow<List<SubjectGradeEntity>> =
        subjectGradeDao.getGradesForStudent(rollNumber)

    fun getInterventionsForStudent(rollNumber: String): Flow<List<InterventionEntity>> =
        interventionDao.getInterventionsForStudent(rollNumber)

    fun getTasksForStudent(rollNumber: String): Flow<List<StudyTaskEntity>> =
        studyTaskDao.getTasksForStudent(rollNumber)

    fun getBadgesForStudent(rollNumber: String): Flow<List<BadgeEntity>> =
        badgeDao.getBadgesForStudent(rollNumber)

    suspend fun initializeSampleDataIfNeeded() = withContext(Dispatchers.IO) {
        val count = studentDao.getCount()
        if (count == 0) {
            studentDao.insertStudents(SampleData.initialStudents)
            subjectGradeDao.insertGrades(SampleData.initialGrades)
            interventionDao.insertInterventions(SampleData.initialInterventions)
            notificationDao.insertNotifications(SampleData.initialNotifications)
            studyTaskDao.insertTasks(SampleData.initialTasks)
            badgeDao.insertBadges(SampleData.initialBadges)
        }
    }

    suspend fun createIntervention(intervention: InterventionEntity) = withContext(Dispatchers.IO) {
        interventionDao.insertIntervention(intervention)
        // Also add a notification
        notificationDao.insertNotification(
            NotificationEntity(
                studentRollNumber = intervention.studentRollNumber,
                title = "📌 New Intervention Assigned: ${intervention.type}",
                message = "Faculty action: ${intervention.actionPlan}. Assigned by ${intervention.assignedFaculty}.",
                type = "INFO"
            )
        )
    }

    suspend fun updateIntervention(intervention: InterventionEntity) = withContext(Dispatchers.IO) {
        interventionDao.updateIntervention(intervention)
        if (intervention.isResolved || intervention.status == "SUCCESSFUL") {
            // Update student's risk and attendance
            val student = studentDao.getStudentByRollDirect(intervention.studentRollNumber)
            if (student != null) {
                val newAttendance = intervention.afterAttendance
                val newRisk = intervention.afterRisk
                val newRiskLevel = AiAnalyticsEngine.getRiskLevel(newRisk).name
                val updatedStudent = student.copy(
                    attendance = newAttendance,
                    riskScore = newRisk,
                    riskLevel = newRiskLevel,
                    successScore = AiAnalyticsEngine.calculateSuccessScore(
                        newAttendance,
                        student.internalMarks,
                        student.assignmentScore,
                        student.engagementScore,
                        student.quizScore
                    )
                )
                studentDao.updateStudent(updatedStudent)
            }
        }
    }

    suspend fun toggleTaskCompleted(taskId: Int, isCompleted: Boolean) = withContext(Dispatchers.IO) {
        studyTaskDao.setTaskCompleted(taskId, isCompleted)
    }

    suspend fun markAllNotificationsAsRead() = withContext(Dispatchers.IO) {
        notificationDao.markAllAsRead()
    }

    suspend fun insertStudent(student: StudentEntity) = withContext(Dispatchers.IO) {
        studentDao.insertStudent(student)
    }

    suspend fun recalculateStudentMetrics(rollNumber: String) = withContext(Dispatchers.IO) {
        val student = studentDao.getStudentByRollDirect(rollNumber) ?: return@withContext
        val newRisk = AiAnalyticsEngine.calculateRiskScore(
            student.attendance,
            student.internalMarks,
            student.assignmentScore,
            student.labScore,
            student.quizScore,
            student.previousGpa,
            student.backlogs,
            student.engagementScore
        )
        val newRiskLevel = AiAnalyticsEngine.getRiskLevel(newRisk).name
        val newSuccessScore = AiAnalyticsEngine.calculateSuccessScore(
            student.attendance,
            student.internalMarks,
            student.assignmentScore,
            student.engagementScore,
            student.quizScore
        )
        val newPredictedGpa = AiAnalyticsEngine.predictSemesterGpa(
            student.currentGpa,
            student.internalMarks,
            student.attendance
        )
        val newCluster = AiAnalyticsEngine.classifyStudentCluster(student).title

        val updated = student.copy(
            riskScore = newRisk,
            riskLevel = newRiskLevel,
            successScore = newSuccessScore,
            predictedGpa = newPredictedGpa,
            cluster = newCluster
        )
        studentDao.updateStudent(updated)
    }
}
