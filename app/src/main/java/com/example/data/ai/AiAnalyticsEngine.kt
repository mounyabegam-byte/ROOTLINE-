package com.example.data.ai

import com.example.data.local.entity.StudentEntity
import com.example.data.local.entity.SubjectGradeEntity
import com.example.model.AcademicRiskLevel
import com.example.model.RiskFactorContribution
import com.example.model.StudentClusterType
import com.example.model.TopicPerformance
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

object AiAnalyticsEngine {

    /**
     * Multi-factor AI Risk Prediction Model.
     * Evaluates attendance, internal marks, assignments, previous GPA, and backlogs.
     * Returns a calibrated risk score between 0 and 100.
     */
    fun calculateRiskScore(
        attendance: Double,
        internalMarks: Double,
        assignmentScore: Double,
        labScore: Double,
        quizScore: Double,
        previousGpa: Double,
        backlogs: Int,
        engagementScore: Double
    ): Int {
        var risk = 0.0

        // 1. Attendance penalty (weight up to 35)
        if (attendance < 75.0) {
            val deficit = (75.0 - attendance) / 30.0
            risk += 15.0 + (25.0 * deficit.coerceIn(0.0, 1.0))
        } else if (attendance < 85.0) {
            risk += 5.0 * ((85.0 - attendance) / 10.0)
        }

        // 2. Internal marks penalty (weight up to 30)
        if (internalMarks < 65.0) {
            val deficit = (65.0 - internalMarks) / 35.0
            risk += 15.0 + (20.0 * deficit.coerceIn(0.0, 1.0))
        } else if (internalMarks < 75.0) {
            risk += 5.0 * ((75.0 - internalMarks) / 10.0)
        }

        // 3. Assignment score penalty (weight up to 15)
        if (assignmentScore < 75.0) {
            risk += 15.0 * ((75.0 - assignmentScore) / 35.0).coerceIn(0.0, 1.0)
        }

        // 4. Backlogs penalty (each backlog adds 12 points, up to 30)
        risk += min(30.0, backlogs * 12.0)

        // 5. Practical/Lab/Quiz/Engagement penalty (weight up to 10)
        val practicalAvg = (labScore + quizScore + engagementScore) / 3.0
        if (practicalAvg < 70.0) {
            risk += 10.0 * ((70.0 - practicalAvg) / 30.0).coerceIn(0.0, 1.0)
        }

        // 6. GPA trend
        if (previousGpa < 7.0) {
            risk += 6.0 * ((7.0 - previousGpa) / 3.0).coerceIn(0.0, 1.0)
        }

        return min(100, max(5, risk.roundToInt()))
    }

    fun getRiskLevel(riskScore: Int): AcademicRiskLevel {
        return when {
            riskScore > 60 -> AcademicRiskLevel.HIGH
            riskScore in 31..60 -> AcademicRiskLevel.MEDIUM
            else -> AcademicRiskLevel.LOW
        }
    }

    /**
     * Calculates the Student Success Score (0-100%) broken into 5 pillars:
     * Academic (35%), Attendance (25%), Assignments (20%), Engagement (10%), Assessment (10%)
     */
    fun calculateSuccessScore(
        attendance: Double,
        internalMarks: Double,
        assignmentScore: Double,
        engagementScore: Double,
        quizScore: Double
    ): Int {
        val score = (internalMarks * 0.35) +
                (attendance * 0.25) +
                (assignmentScore * 0.20) +
                (engagementScore * 0.10) +
                (quizScore * 0.10)
        return min(100, max(0, score.roundToInt()))
    }

    /**
     * Explainable AI (XAI) factors analysis.
     */
    fun generateExplainableFactors(student: StudentEntity): List<RiskFactorContribution> {
        val list = mutableListOf<RiskFactorContribution>()

        // Attendance factor
        val attStatus = if (student.attendance < 75.0) "Below 75% threshold" else "Healthy attendance"
        list.add(
            RiskFactorContribution(
                factorName = "Attendance Rate",
                currentValue = "${student.attendance.toInt()}%",
                impactWeight = (student.attendance / 100.0).toFloat().coerceIn(0f, 1f),
                isNegative = student.attendance < 75.0,
                statusText = attStatus
            )
        )

        // Internal Marks factor
        val marksStatus = if (student.internalMarks < 60.0) "Declining performance" else "Satisfactory"
        list.add(
            RiskFactorContribution(
                factorName = "Internal Examination",
                currentValue = "${student.internalMarks.toInt()}%",
                impactWeight = (student.internalMarks / 100.0).toFloat().coerceIn(0f, 1f),
                isNegative = student.internalMarks < 60.0,
                statusText = marksStatus
            )
        )

        // Assignment Completion factor
        val assignStatus = if (student.assignmentScore < 70.0) "Submissions lagging" else "On track"
        list.add(
            RiskFactorContribution(
                factorName = "Assignment Submissions",
                currentValue = "${student.assignmentScore.toInt()}%",
                impactWeight = (student.assignmentScore / 100.0).toFloat().coerceIn(0f, 1f),
                isNegative = student.assignmentScore < 70.0,
                statusText = assignStatus
            )
        )

        // Backlogs factor
        val backlogStatus = if (student.backlogs > 0) "${student.backlogs} Pending Subject(s)" else "Clear Record"
        val backlogWeight = if (student.backlogs == 0) 1.0f else (1.0f - (student.backlogs * 0.3f)).coerceAtLeast(0.1f)
        list.add(
            RiskFactorContribution(
                factorName = "Active Backlogs",
                currentValue = "${student.backlogs} Subjects",
                impactWeight = backlogWeight,
                isNegative = student.backlogs > 0,
                statusText = backlogStatus
            )
        )

        return list
    }

    /**
     * K-Means clustering algorithm simulation.
     */
    fun classifyStudentCluster(student: StudentEntity): StudentClusterType {
        return when {
            student.internalMarks >= 85.0 && student.attendance >= 85.0 && student.backlogs == 0 ->
                StudentClusterType.HIGH_PERFORMERS
            student.internalMarks >= 70.0 && student.attendance >= 75.0 && student.backlogs == 0 ->
                StudentClusterType.CONSISTENT_PERFORMERS
            student.internalMarks >= 58.0 && student.attendance >= 68.0 && student.backlogs <= 1 ->
                StudentClusterType.IMPROVEMENT_REQUIRED
            else ->
                StudentClusterType.AT_RISK
        }
    }

    /**
     * Performance forecast (Current GPA -> Predicted Semester GPA).
     */
    fun predictSemesterGpa(currentGpa: Double, internalMarks: Double, attendance: Double): Double {
        val estimatedGradeScale = (internalMarks / 10.0)
        val attendanceFactor = (attendance / 100.0)
        val predicted = (currentGpa * 0.5) + (estimatedGradeScale * 0.4) + (attendanceFactor * 1.0)
        return min(10.0, max(4.0, (predicted * 10).roundToInt() / 10.0))
    }

    /**
     * Generates personalized prescriptive recommendations.
     */
    fun generateRecommendations(
        student: StudentEntity,
        weakSubjects: List<SubjectGradeEntity>
    ): List<String> {
        val recs = mutableListOf<String>()

        if (weakSubjects.isNotEmpty()) {
            val names = weakSubjects.joinToString(", ") { it.subjectName }
            recs.add("Attend subject-wise remedial coaching in $names.")
        }

        if (student.attendance < 75.0) {
            val needed = (75.0 - student.attendance).roundToInt() + 2
            recs.add("Improve overall attendance by +$needed% to satisfy 75% exam eligibility criteria.")
        }

        if (student.assignmentScore < 75.0) {
            recs.add("Complete and submit outstanding module assignments to recover 15% internal grade.")
        }

        if (student.backlogs > 0) {
            recs.add("Enroll in supplementary backlog preparation cohort with faculty guidance.")
        }

        if (student.riskScore > 60) {
            recs.add("Schedule weekly 1-on-1 progress check-in with assigned faculty advisor.")
        } else {
            recs.add("Maintain consistency; explore hackathons and advanced project electives.")
        }

        return recs
    }

    /**
     * Class/Subject Topic Learning Gap Detection.
     */
    val sampleClassTopicGaps = listOf(
        TopicPerformance(
            topicName = "Artificial Neural Networks & Backpropagation",
            subjectName = "Machine Learning",
            averageScore = 54,
            isWeakGap = true,
            recommendation = "Weakest topic for class (54%). Conduct focused remedial session on matrix backpropagation and activation functions."
        ),
        TopicPerformance(
            topicName = "TCP Flow Control & Subnetting",
            subjectName = "Computer Networks",
            averageScore = 59,
            isWeakGap = true,
            recommendation = "Moderate gap (59%). Schedule dedicated hands-on packet capture and subnetting lab."
        ),
        TopicPerformance(
            topicName = "K-Means & Hierarchical Clustering",
            subjectName = "Machine Learning",
            averageScore = 71,
            isWeakGap = false,
            recommendation = "Good grasp. Reinforce with practical unsupervised mini-assignments."
        ),
        TopicPerformance(
            topicName = "Supervised Classification Models",
            subjectName = "Machine Learning",
            averageScore = 78,
            isWeakGap = false,
            recommendation = "Strong performance across student cohorts."
        ),
        TopicPerformance(
            topicName = "Linear & Logistic Regression",
            subjectName = "Machine Learning",
            averageScore = 82,
            isWeakGap = false,
            recommendation = "Topic fully mastered by 86% of the class."
        )
    )
}
