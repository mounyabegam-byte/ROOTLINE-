package com.example.data.firebase

import android.content.Context
import android.util.Log
import com.example.data.local.entity.InterventionEntity
import com.example.data.local.entity.NotificationEntity
import com.example.data.local.entity.StudentEntity
import com.example.data.local.entity.SubjectGradeEntity
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class DatabaseService(private val context: Context) {

    private val firestore: FirebaseFirestore? by lazy {
        try {
            if (FirebaseApp.getApps(context).isNotEmpty()) {
                FirebaseFirestore.getInstance()
            } else {
                null
            }
        } catch (e: Exception) {
            Log.w("DatabaseService", "Firebase not initialized, local mode active", e)
            null
        }
    }

    val isFirebaseAvailable: Boolean
        get() = firestore != null

    /**
     * Real-time listener for students collection
     */
    fun listenToStudentsRealtime(): Flow<List<StudentEntity>> = callbackFlow {
        val db = firestore
        if (db == null) {
            close()
            return@callbackFlow
        }

        val registration: ListenerRegistration = db.collection("students")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w("DatabaseService", "Firestore students listen error", error)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val students = snapshot.documents.mapNotNull { doc ->
                        try {
                            StudentEntity(
                                rollNumber = doc.getString("rollNumber") ?: doc.id,
                                name = doc.getString("name") ?: "Student",
                                department = doc.getString("department") ?: "CSE",
                                year = (doc.getLong("year") ?: 3L).toInt(),
                                semester = (doc.getLong("semester") ?: 5L).toInt(),
                                section = doc.getString("section") ?: "A",
                                email = doc.getString("email") ?: "",
                                phone = doc.getString("phone") ?: "",
                                attendance = doc.getDouble("attendance") ?: 75.0,
                                internalMarks = doc.getDouble("internalMarks") ?: 70.0,
                                assignmentScore = doc.getDouble("assignmentScore") ?: 75.0,
                                labScore = doc.getDouble("labScore") ?: 70.0,
                                quizScore = doc.getDouble("quizScore") ?: 70.0,
                                previousGpa = doc.getDouble("previousGpa") ?: 7.0,
                                currentGpa = doc.getDouble("currentGpa") ?: 7.0,
                                predictedGpa = doc.getDouble("predictedGpa") ?: 7.2,
                                backlogs = (doc.getLong("backlogs") ?: 0L).toInt(),
                                engagementScore = doc.getDouble("engagementScore") ?: 70.0,
                                riskScore = (doc.getLong("riskScore") ?: 30L).toInt(),
                                riskLevel = doc.getString("riskLevel") ?: "LOW",
                                successScore = (doc.getLong("successScore") ?: 75L).toInt(),
                                cluster = doc.getString("cluster") ?: "Consistent Performers",
                                advisorName = doc.getString("advisorName") ?: "Faculty Advisor"
                            )
                        } catch (e: Exception) {
                            null
                        }
                    }
                    trySend(students)
                }
            }

        awaitClose { registration.remove() }
    }

    /**
     * Real-time listener for interventions collection
     */
    fun listenToInterventionsRealtime(): Flow<List<InterventionEntity>> = callbackFlow {
        val db = firestore
        if (db == null) {
            close()
            return@callbackFlow
        }

        val registration = db.collection("interventions")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w("DatabaseService", "Firestore interventions listen error", error)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val list = snapshot.documents.mapNotNull { doc ->
                        try {
                            InterventionEntity(
                                id = (doc.getLong("id") ?: doc.id.hashCode().toLong()).toInt(),
                                studentRollNumber = doc.getString("studentRollNumber") ?: "",
                                studentName = doc.getString("studentName") ?: "",
                                department = doc.getString("department") ?: "CSE",
                                type = doc.getString("type") ?: "Academic Support",
                                issue = doc.getString("issue") ?: "",
                                actionPlan = doc.getString("actionPlan") ?: "",
                                assignedFaculty = doc.getString("assignedFaculty") ?: "",
                                deadline = doc.getString("deadline") ?: "",
                                status = doc.getString("status") ?: "IN_PROGRESS",
                                beforeRisk = (doc.getLong("beforeRisk") ?: 75L).toInt(),
                                afterRisk = (doc.getLong("afterRisk") ?: 50L).toInt(),
                                beforeAttendance = doc.getDouble("beforeAttendance") ?: 65.0,
                                afterAttendance = doc.getDouble("afterAttendance") ?: 75.0,
                                isResolved = doc.getBoolean("isResolved") ?: false,
                                notes = doc.getString("notes") ?: ""
                            )
                        } catch (e: Exception) {
                            null
                        }
                    }
                    trySend(list)
                }
            }

        awaitClose { registration.remove() }
    }

    /**
     * Upsert student document
     */
    suspend fun saveStudent(student: StudentEntity) {
        val db = firestore ?: return
        try {
            val map = mapOf(
                "rollNumber" to student.rollNumber,
                "name" to student.name,
                "department" to student.department,
                "year" to student.year,
                "semester" to student.semester,
                "section" to student.section,
                "email" to student.email,
                "phone" to student.phone,
                "attendance" to student.attendance,
                "internalMarks" to student.internalMarks,
                "assignmentScore" to student.assignmentScore,
                "labScore" to student.labScore,
                "quizScore" to student.quizScore,
                "previousGpa" to student.previousGpa,
                "currentGpa" to student.currentGpa,
                "predictedGpa" to student.predictedGpa,
                "backlogs" to student.backlogs,
                "engagementScore" to student.engagementScore,
                "riskScore" to student.riskScore,
                "riskLevel" to student.riskLevel,
                "successScore" to student.successScore,
                "cluster" to student.cluster,
                "advisorName" to student.advisorName,
                "updatedAt" to System.currentTimeMillis()
            )
            db.collection("students").document(student.rollNumber).set(map, SetOptions.merge()).await()
        } catch (e: Exception) {
            Log.e("DatabaseService", "Failed to save student to Firestore", e)
        }
    }

    /**
     * Save intervention document
     */
    suspend fun saveIntervention(intervention: InterventionEntity) {
        val db = firestore ?: return
        try {
            val idStr = if (intervention.id != 0) intervention.id.toString() else "int_${System.currentTimeMillis()}"
            val map = mapOf(
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
            db.collection("interventions").document(idStr).set(map, SetOptions.merge()).await()
        } catch (e: Exception) {
            Log.e("DatabaseService", "Failed to save intervention to Firestore", e)
        }
    }

    /**
     * Delete student document
     */
    suspend fun deleteStudent(rollNumber: String) {
        val db = firestore ?: return
        try {
            db.collection("students").document(rollNumber).delete().await()
        } catch (e: Exception) {
            Log.e("DatabaseService", "Failed to delete student", e)
        }
    }
}
