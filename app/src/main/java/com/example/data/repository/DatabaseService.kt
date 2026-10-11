package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.data.ai.AiAnalyticsEngine
import com.example.data.local.AppDatabase
import com.example.data.local.entity.InterventionEntity
import com.example.data.local.entity.StudentEntity
import com.example.data.local.entity.SubjectGradeEntity
import com.example.model.AttendanceRecord
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DatabaseService(
    private val context: Context,
    private val database: AppDatabase,
    private val scope: CoroutineScope
) {
    private val studentDao = database.studentDao()
    private val subjectGradeDao = database.subjectGradeDao()
    private val interventionDao = database.interventionDao()
    private val notificationDao = database.notificationDao()

    private val firestore: FirebaseFirestore? by lazy {
        try {
            if (FirebaseApp.getApps(context).isNotEmpty()) {
                FirebaseFirestore.getInstance()
            } else {
                null
            }
        } catch (e: Exception) {
            Log.w("DatabaseService", "Firestore not initialized: ${e.message}")
            null
        }
    }

    // In-memory real-time attendance records flow
    private val _attendanceRecords = MutableStateFlow<Map<String, List<AttendanceRecord>>>(emptyMap())
    val attendanceRecords = _attendanceRecords.asStateFlow()

    init {
        setupFirestoreRealtimeListeners()
    }

    // ==========================================
    // 1. STUDENT MARKS CRUD & SYNC
    // ==========================================

    fun getMarksRealtime(studentRoll: String): Flow<List<SubjectGradeEntity>> {
        return subjectGradeDao.getGradesForStudent(studentRoll)
    }

    suspend fun createMark(grade: SubjectGradeEntity) = withContext(Dispatchers.IO) {
        subjectGradeDao.insertGrades(listOf(grade))
        syncMarkToFirestore(grade)
        recalculateStudentTelemetry(grade.studentRollNumber)
    }

    suspend fun updateMark(
        gradeId: Int,
        studentRoll: String,
        subjectCode: String,
        internalMarks: Double,
        assignmentMarks: Double,
        labMarks: Double
    ) = withContext(Dispatchers.IO) {
        val existingGrades = subjectGradeDao.getGradesForStudentDirect(studentRoll)
        val target = existingGrades.firstOrNull { it.id == gradeId || it.subjectCode == subjectCode }
        if (target != null) {
            val overall = (internalMarks * 0.4) + (assignmentMarks * 0.3) + (labMarks * 0.3)
            val aiStatus = when {
                overall >= 85.0 -> "EXCELLENT"
                overall >= 70.0 -> "GOOD"
                overall >= 55.0 -> "NEEDS_ATTENTION"
                else -> "CRITICAL"
            }
            val updated = target.copy(
                internalMarks = internalMarks,
                assignmentMarks = assignmentMarks,
                labMarks = labMarks,
                overallScore = overall,
                aiStatus = aiStatus
            )
            subjectGradeDao.updateGrade(updated)
            syncMarkToFirestore(updated)
            recalculateStudentTelemetry(studentRoll)
        }
    }

    suspend fun deleteMark(gradeId: Int, studentRoll: String, subjectCode: String) = withContext(Dispatchers.IO) {
        // Delete in Firestore
        firestore?.collection("marks")?.document("${studentRoll}_$subjectCode")?.delete()
        recalculateStudentTelemetry(studentRoll)
    }

    // ==========================================
    // 2. ATTENDANCE RECORDS CRUD & SYNC
    // ==========================================

    fun getAttendanceForStudent(studentRoll: String): List<AttendanceRecord> {
        val map = _attendanceRecords.value
        return map[studentRoll] ?: listOf(
            AttendanceRecord(studentRollNumber = studentRoll, subjectCode = "CS301", subjectName = "Computer Networks", totalClasses = 45, attendedClasses = 31),
            AttendanceRecord(studentRollNumber = studentRoll, subjectCode = "CS302", subjectName = "Machine Learning", totalClasses = 42, attendedClasses = 35),
            AttendanceRecord(studentRollNumber = studentRoll, subjectCode = "CS303", subjectName = "Java Programming", totalClasses = 38, attendedClasses = 32),
            AttendanceRecord(studentRollNumber = studentRoll, subjectCode = "CS304", subjectName = "DBMS", totalClasses = 40, attendedClasses = 33),
            AttendanceRecord(studentRollNumber = studentRoll, subjectCode = "CS305", subjectName = "Artificial Intelligence", totalClasses = 36, attendedClasses = 32)
        )
    }

    suspend fun recordAttendance(
        studentRoll: String,
        subjectCode: String,
        subjectName: String,
        attendedCount: Int,
        totalCount: Int
    ) = withContext(Dispatchers.IO) {
        val record = AttendanceRecord(
            studentRollNumber = studentRoll,
            subjectCode = subjectCode,
            subjectName = subjectName,
            totalClasses = totalCount,
            attendedClasses = attendedCount
        )

        // Update in-memory
        val currentMap = _attendanceRecords.value.toMutableMap()
        val list = currentMap[studentRoll]?.toMutableList() ?: mutableListOf()
        list.removeAll { it.subjectCode == subjectCode }
        list.add(record)
        currentMap[studentRoll] = list
        _attendanceRecords.value = currentMap

        // Update overall student attendance in Room
        val avgAtt = if (list.isNotEmpty()) list.map { it.percentage }.average() else record.percentage
        val student = studentDao.getStudentByRollDirect(studentRoll)
        if (student != null) {
            val updated = student.copy(attendance = avgAtt)
            studentDao.updateStudent(updated)
            recalculateStudentTelemetry(studentRoll)
        }

        // Sync to Firestore
        syncAttendanceToFirestore(record)
    }

    // ==========================================
    // 3. INTERVENTION LOGS CRUD & SYNC
    // ==========================================

    fun getInterventionsRealtime(): Flow<List<InterventionEntity>> {
        return interventionDao.getAllInterventions()
    }

    fun getInterventionsForStudentRealtime(studentRoll: String): Flow<List<InterventionEntity>> {
        return interventionDao.getInterventionsForStudent(studentRoll)
    }

    suspend fun createIntervention(intervention: InterventionEntity): Long = withContext(Dispatchers.IO) {
        val id = interventionDao.insertIntervention(intervention)
        val created = intervention.copy(id = id.toInt())
        syncInterventionToFirestore(created)
        id
    }

    suspend fun updateIntervention(intervention: InterventionEntity) = withContext(Dispatchers.IO) {
        interventionDao.updateIntervention(intervention)
        syncInterventionToFirestore(intervention)
    }

    suspend fun deleteIntervention(id: Int) = withContext(Dispatchers.IO) {
        interventionDao.deleteIntervention(id)
        firestore?.collection("interventions")?.document(id.toString())?.delete()
    }

    suspend fun resolveIntervention(
        intervention: InterventionEntity,
        afterRisk: Int,
        afterAttendance: Double
    ) = withContext(Dispatchers.IO) {
        val updated = intervention.copy(
            status = "SUCCESSFUL",
            isResolved = true,
            afterRisk = afterRisk,
            afterAttendance = afterAttendance,
            notes = "Target risk reduction achieved. Remedial curriculum completed."
        )
        interventionDao.updateIntervention(updated)
        syncInterventionToFirestore(updated)

        // Update student
        val student = studentDao.getStudentByRollDirect(intervention.studentRollNumber)
        if (student != null) {
            val updatedStudent = student.copy(
                riskScore = afterRisk,
                riskLevel = AiAnalyticsEngine.getRiskLevel(afterRisk).name,
                attendance = afterAttendance
            )
            studentDao.updateStudent(updatedStudent)
        }
    }

    // ==========================================
    // FIRESTORE SYNCHRONIZATION HELPERS
    // ==========================================

    private fun setupFirestoreRealtimeListeners() {
        val db = firestore ?: return
        try {
            // 1. Listen to marks
            db.collection("marks").addSnapshotListener { snapshots, e ->
                if (e != null || snapshots == null) return@addSnapshotListener
                scope.launch(Dispatchers.IO) {
                    for (doc in snapshots.documents) {
                        val roll = doc.getString("studentRollNumber") ?: continue
                        val code = doc.getString("subjectCode") ?: continue
                        val name = doc.getString("subjectName") ?: "Subject"
                        val internal = doc.getDouble("internalMarks") ?: 70.0
                        val assign = doc.getDouble("assignmentMarks") ?: 75.0
                        val lab = doc.getDouble("labMarks") ?: 70.0
                        val att = doc.getDouble("attendance") ?: 80.0
                        val overall = doc.getDouble("overallScore") ?: 72.0
                        val status = doc.getString("aiStatus") ?: "GOOD"

                        val entity = SubjectGradeEntity(
                            studentRollNumber = roll,
                            subjectName = name,
                            subjectCode = code,
                            internalMarks = internal,
                            assignmentMarks = assign,
                            labMarks = lab,
                            attendance = att,
                            overallScore = overall,
                            credits = 4,
                            aiStatus = status
                        )
                        subjectGradeDao.insertGrades(listOf(entity))
                    }
                }
            }

            // 2. Listen to interventions
            db.collection("interventions").addSnapshotListener { snapshots, e ->
                if (e != null || snapshots == null) return@addSnapshotListener
                scope.launch(Dispatchers.IO) {
                    for (doc in snapshots.documents) {
                        val roll = doc.getString("studentRollNumber") ?: continue
                        val name = doc.getString("studentName") ?: "Student"
                        val dept = doc.getString("department") ?: "CSE"
                        val type = doc.getString("type") ?: "Remedial"
                        val issue = doc.getString("issue") ?: ""
                        val plan = doc.getString("actionPlan") ?: ""
                        val faculty = doc.getString("assignedFaculty") ?: ""
                        val deadline = doc.getString("deadline") ?: ""
                        val status = doc.getString("status") ?: "IN_PROGRESS"
                        val beforeRisk = doc.getLong("beforeRisk")?.toInt() ?: 70
                        val afterRisk = doc.getLong("afterRisk")?.toInt() ?: 50
                        val beforeAtt = doc.getDouble("beforeAttendance") ?: 65.0
                        val afterAtt = doc.getDouble("afterAttendance") ?: 75.0
                        val resolved = doc.getBoolean("isResolved") ?: false

                        val entity = InterventionEntity(
                            id = doc.id.toIntOrNull() ?: 0,
                            studentRollNumber = roll,
                            studentName = name,
                            department = dept,
                            type = type,
                            issue = issue,
                            actionPlan = plan,
                            assignedFaculty = faculty,
                            deadline = deadline,
                            status = status,
                            beforeRisk = beforeRisk,
                            afterRisk = afterRisk,
                            beforeAttendance = beforeAtt,
                            afterAttendance = afterAtt,
                            isResolved = resolved
                        )
                        interventionDao.insertInterventions(listOf(entity))
                    }
                }
            }
        } catch (e: Exception) {
            Log.w("DatabaseService", "Error setting up firestore listeners: ${e.message}")
        }
    }

    private fun syncMarkToFirestore(grade: SubjectGradeEntity) {
        val db = firestore ?: return
        scope.launch(Dispatchers.IO) {
            try {
                val data = hashMapOf(
                    "studentRollNumber" to grade.studentRollNumber,
                    "subjectCode" to grade.subjectCode,
                    "subjectName" to grade.subjectName,
                    "internalMarks" to grade.internalMarks,
                    "assignmentMarks" to grade.assignmentMarks,
                    "labMarks" to grade.labMarks,
                    "attendance" to grade.attendance,
                    "overallScore" to grade.overallScore,
                    "credits" to grade.credits,
                    "aiStatus" to grade.aiStatus,
                    "syncedAt" to System.currentTimeMillis()
                )
                db.collection("marks")
                    .document("${grade.studentRollNumber}_${grade.subjectCode}")
                    .set(data, SetOptions.merge())
            } catch (e: Exception) {
                Log.w("DatabaseService", "Failed to sync mark to Firestore: ${e.message}")
            }
        }
    }

    private fun syncAttendanceToFirestore(record: AttendanceRecord) {
        val db = firestore ?: return
        scope.launch(Dispatchers.IO) {
            try {
                val data = hashMapOf(
                    "studentRollNumber" to record.studentRollNumber,
                    "subjectCode" to record.subjectCode,
                    "subjectName" to record.subjectName,
                    "totalClasses" to record.totalClasses,
                    "attendedClasses" to record.attendedClasses,
                    "percentage" to record.percentage,
                    "lastUpdated" to record.lastUpdated
                )
                db.collection("attendance")
                    .document("${record.studentRollNumber}_${record.subjectCode}")
                    .set(data, SetOptions.merge())
            } catch (e: Exception) {
                Log.w("DatabaseService", "Failed to sync attendance to Firestore: ${e.message}")
            }
        }
    }

    private fun syncInterventionToFirestore(intervention: InterventionEntity) {
        val db = firestore ?: return
        scope.launch(Dispatchers.IO) {
            try {
                val data = hashMapOf(
                    "id" to intervention.id,
                    "studentRollNumber" to intervention.studentRollNumber,
                    "studentName" to intervention.studentName,
                    "department" to intervention.department,
                    "type" to intervention.type,
                    "issue" to intervention.issue,
                    "actionPlan" to intervention.actionPlan,
                    "assignedFaculty" to intervention.assignedFaculty,
                    "deadline" to intervention.deadline,
                    "status" to intervention.status,
                    "beforeRisk" to intervention.beforeRisk,
                    "afterRisk" to intervention.afterRisk,
                    "beforeAttendance" to intervention.beforeAttendance,
                    "afterAttendance" to intervention.afterAttendance,
                    "isResolved" to intervention.isResolved,
                    "notes" to intervention.notes,
                    "createdAt" to intervention.createdAt
                )
                val docId = if (intervention.id > 0) intervention.id.toString() else "${intervention.studentRollNumber}_${System.currentTimeMillis()}"
                db.collection("interventions")
                    .document(docId)
                    .set(data, SetOptions.merge())
            } catch (e: Exception) {
                Log.w("DatabaseService", "Failed to sync intervention to Firestore: ${e.message}")
            }
        }
    }

    private suspend fun recalculateStudentTelemetry(studentRoll: String) {
        val student = studentDao.getStudentByRollDirect(studentRoll) ?: return
        val grades = subjectGradeDao.getGradesForStudentDirect(studentRoll)
        if (grades.isNotEmpty()) {
            val avgInternal = grades.map { it.internalMarks }.average()
            val avgAssign = grades.map { it.assignmentMarks }.average()
            val avgLab = grades.map { it.labMarks }.average()
            val avgAtt = grades.map { it.attendance }.average()

            val risk = AiAnalyticsEngine.calculateRiskScore(
                attendance = avgAtt,
                internalMarks = avgInternal,
                assignmentScore = avgAssign,
                labScore = avgLab,
                quizScore = student.quizScore,
                previousGpa = student.previousGpa,
                backlogs = student.backlogs,
                engagementScore = student.engagementScore
            )
            val success = AiAnalyticsEngine.calculateSuccessScore(
                attendance = avgAtt,
                internalMarks = avgInternal,
                assignmentScore = avgAssign,
                engagementScore = student.engagementScore,
                quizScore = student.quizScore
            )
            val updated = student.copy(
                internalMarks = avgInternal,
                assignmentScore = avgAssign,
                labScore = avgLab,
                attendance = avgAtt,
                riskScore = risk,
                riskLevel = AiAnalyticsEngine.getRiskLevel(risk).name,
                successScore = success
            )
            studentDao.updateStudent(updated)
        }
    }
}
