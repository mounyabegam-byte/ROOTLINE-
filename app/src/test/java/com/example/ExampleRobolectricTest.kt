package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.ai.AiAnalyticsEngine
import com.example.data.local.SampleData
import com.example.model.AcademicRiskLevel
import com.example.model.StudentClusterType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("EduPulse AI", appName)
    }

    @Test
    fun `test risk calculation model for high risk student`() {
        // Attendance 68%, Internal 54%, Assignments 61%, 2 Backlogs
        val risk = AiAnalyticsEngine.calculateRiskScore(
            attendance = 68.0,
            internalMarks = 54.0,
            assignmentScore = 61.0,
            labScore = 59.0,
            quizScore = 56.0,
            previousGpa = 6.4,
            backlogs = 2,
            engagementScore = 52.0
        )
        val level = AiAnalyticsEngine.getRiskLevel(risk)
        assertTrue("Risk score should be > 60 for low attendance + backlogs", risk > 60)
        assertEquals(AcademicRiskLevel.HIGH, level)
    }

    @Test
    fun `test student clustering for high performer`() {
        val student = SampleData.initialStudents.first { it.rollNumber == "21A91A0502" } // Priya Sharma
        val cluster = AiAnalyticsEngine.classifyStudentCluster(student)
        assertEquals(StudentClusterType.HIGH_PERFORMERS, cluster)
    }

    @Test
    fun `test student success score computation`() {
        val success = AiAnalyticsEngine.calculateSuccessScore(
            attendance = 94.0,
            internalMarks = 89.0,
            assignmentScore = 96.0,
            engagementScore = 95.0,
            quizScore = 94.0
        )
        assertTrue("Success score should be high for top student", success >= 90)
    }

    @Test
    fun `test auth view model session persistence across app restarts`() = kotlinx.coroutines.runBlocking {
        val app = ApplicationProvider.getApplicationContext<android.app.Application>()

        val firstAuthViewModel = com.example.ui.auth.AuthViewModel(app)
        val result = firstAuthViewModel.suspendSignup(
            name = "Test Student",
            email = "test.student@college.edu",
            pass = "Pass1234!",
            role = com.example.model.UserRole.STUDENT,
            department = "CSE",
            rollNumber = "21A91A0599"
        )
        org.junit.Assert.assertTrue(result.isSuccess)

        val savedUser = firstAuthViewModel.currentUser.value
        org.junit.Assert.assertNotNull(savedUser)
        assertEquals("test.student@college.edu", savedUser?.email)
        assertEquals(com.example.model.UserRole.STUDENT, savedUser?.role)

        // Simulate app restart by instantiating a new AuthViewModel instance
        val restartedAuthViewModel = com.example.ui.auth.AuthViewModel(app)
        val restoredUser = restartedAuthViewModel.currentUser.value

        org.junit.Assert.assertNotNull("User should persist across app restart", restoredUser)
        assertEquals("test.student@college.edu", restoredUser?.email)
        assertEquals(com.example.model.UserRole.STUDENT, restoredUser?.role)
        assertEquals(com.example.model.UserRole.STUDENT, restartedAuthViewModel.currentRole.value)

        // Test Logout
        restartedAuthViewModel.logout()
        org.junit.Assert.assertNull(restartedAuthViewModel.currentUser.value)

        // Verify after logout, next restart is unauthenticated
        val nextRestartAuthViewModel = com.example.ui.auth.AuthViewModel(app)
        org.junit.Assert.assertNull(nextRestartAuthViewModel.currentUser.value)
    }
}
