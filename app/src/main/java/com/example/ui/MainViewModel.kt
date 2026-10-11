package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ai.AiAnalyticsEngine
import com.example.data.ai.GeminiAiAssistant
import com.example.data.auth.AuthUser
import com.example.data.auth.FirebaseAuthService
import com.example.data.auth.UserRoleProvider
import com.example.data.local.AppDatabase
import com.example.data.local.entity.BadgeEntity
import com.example.data.local.entity.InterventionEntity
import com.example.data.local.entity.NotificationEntity
import com.example.data.local.entity.StudentEntity
import com.example.data.local.entity.StudyTaskEntity
import com.example.data.local.entity.SubjectGradeEntity
import com.example.data.repository.DatabaseService
import com.example.data.repository.StudentRepository
import com.example.model.ChatMessage
import com.example.model.UserRole
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val repository = StudentRepository(db)
    private val geminiAssistant = GeminiAiAssistant()
    private val authService = FirebaseAuthService(application)

    // User Role Provider
    val userRoleProvider = UserRoleProvider(application, viewModelScope)

    // Database Service (Real-time Firestore CRUD & Sync Layer)
    val databaseService = DatabaseService(application, db, viewModelScope)

    // Auth State - Session Persistence
    private val _currentUser = MutableStateFlow<AuthUser?>(authService.getPersistedUser())
    val currentUser: StateFlow<AuthUser?> = _currentUser.asStateFlow()

    private val _isLoadingAuth = MutableStateFlow(false)
    val isLoadingAuth: StateFlow<Boolean> = _isLoadingAuth.asStateFlow()

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    // Role state dynamically connected to UserRoleProvider
    val currentRole: StateFlow<UserRole> = userRoleProvider.currentRole

    // Selected student roll number for student view & drilldown
    private val _selectedRollNumber = MutableStateFlow(
        _currentUser.value?.rollNumber ?: "21A91A0501"
    )
    val selectedRollNumber: StateFlow<String> = _selectedRollNumber.asStateFlow()

    init {
        _currentUser.value?.let {
            userRoleProvider.setUser(it)
        }
        viewModelScope.launch {
            repository.initializeSampleDataIfNeeded()
        }
    }

    // Filter states
    val searchQuery = MutableStateFlow("")
    val filterDept = MutableStateFlow("ALL")
    val filterRisk = MutableStateFlow("ALL")

    val allStudents: StateFlow<List<StudentEntity>> = repository.allStudents
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredStudents: StateFlow<List<StudentEntity>> = combine(
        allStudents,
        searchQuery,
        filterDept,
        filterRisk
    ) { students, query, dept, risk ->
        students.filter { student ->
            val matchesQuery = query.isBlank() ||
                    student.name.contains(query, ignoreCase = true) ||
                    student.rollNumber.contains(query, ignoreCase = true)
            val matchesDept = dept == "ALL" || student.department == dept
            val matchesRisk = risk == "ALL" || student.riskLevel == risk
            matchesQuery && matchesDept && matchesRisk
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val currentStudent: StateFlow<StudentEntity?> = _selectedRollNumber.flatMapLatest { roll ->
        repository.getStudent(roll)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val currentStudentGrades: StateFlow<List<SubjectGradeEntity>> = _selectedRollNumber.flatMapLatest { roll ->
        databaseService.getMarksRealtime(roll)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allInterventions: StateFlow<List<InterventionEntity>> = databaseService.getInterventionsRealtime()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val currentStudentInterventions: StateFlow<List<InterventionEntity>> = _selectedRollNumber.flatMapLatest { roll ->
        databaseService.getInterventionsForStudentRealtime(roll)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allNotifications: StateFlow<List<NotificationEntity>> = repository.allNotifications
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val currentStudentTasks: StateFlow<List<StudyTaskEntity>> = _selectedRollNumber.flatMapLatest { roll ->
        repository.getTasksForStudent(roll)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val currentStudentBadges: StateFlow<List<BadgeEntity>> = _selectedRollNumber.flatMapLatest { roll ->
        repository.getBadgesForStudent(roll)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // AI Assistant Chat State
    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                text = "Hello! I am EduPulse AI, your academic intelligence advisor. Ask me anything about risk factors, subject diagnostics, attendance plans, or personalized study pathways.",
                isFromUser = false,
                quickActions = listOf(
                    "Why is my risk score high?",
                    "How to improve Computer Networks?",
                    "Generate 7-day study plan",
                    "Attendance recovery plan"
                )
            )
        )
    )
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _isGeneratingAi = MutableStateFlow(false)
    val isGeneratingAi: StateFlow<Boolean> = _isGeneratingAi.asStateFlow()

    fun login(email: String, pass: String) {
        _isLoadingAuth.value = true
        _authError.value = null

        viewModelScope.launch {
            val result = authService.signIn(email, pass)
            result.fold(
                onSuccess = { user ->
                    _currentUser.value = user
                    userRoleProvider.setUser(user)
                    user.rollNumber?.let { _selectedRollNumber.value = it }
                    _isLoadingAuth.value = false
                },
                onFailure = { error ->
                    _authError.value = error.message ?: "Authentication failed."
                    _isLoadingAuth.value = false
                }
            )
        }
    }

    fun signup(name: String, email: String, pass: String, role: UserRole, dept: String, roll: String?) {
        _isLoadingAuth.value = true
        _authError.value = null

        viewModelScope.launch {
            val result = authService.signUp(name, email, pass, role, dept, roll)
            result.fold(
                onSuccess = { user ->
                    _currentUser.value = user
                    userRoleProvider.setUser(user)
                    user.rollNumber?.let { _selectedRollNumber.value = it }
                    _isLoadingAuth.value = false
                },
                onFailure = { error ->
                    _authError.value = error.message ?: "Signup failed."
                    _isLoadingAuth.value = false
                }
            )
        }
    }

    fun sendPasswordReset(email: String) {
        viewModelScope.launch {
            authService.sendPasswordReset(email)
        }
    }

    fun logout() {
        authService.signOut()
        _currentUser.value = null
        userRoleProvider.setUser(null)
        _authError.value = null
    }

    fun switchRole(role: UserRole) {
        userRoleProvider.setRole(role)
    }

    fun selectStudent(rollNumber: String) {
        _selectedRollNumber.value = rollNumber
    }

    // CRUD: Marks Update via DatabaseService
    fun updateStudentMarks(
        roll: String,
        code: String,
        internal: Double,
        assign: Double,
        lab: Double
    ) {
        viewModelScope.launch {
            databaseService.updateMark(
                gradeId = 0,
                studentRoll = roll,
                subjectCode = code,
                internalMarks = internal,
                assignmentMarks = assign,
                labMarks = lab
            )
        }
    }

    // CRUD: Attendance Update via DatabaseService
    fun recordStudentAttendance(
        roll: String,
        code: String,
        attended: Int,
        total: Int
    ) {
        viewModelScope.launch {
            val subjectName = currentStudentGrades.value.firstOrNull { it.subjectCode == code }?.subjectName ?: "Course Subject"
            databaseService.recordAttendance(
                studentRoll = roll,
                subjectCode = code,
                subjectName = subjectName,
                attendedCount = attended,
                totalCount = total
            )
        }
    }

    fun sendChatMessage(prompt: String) {
        if (prompt.isBlank()) return
        val userMsg = ChatMessage(text = prompt, isFromUser = true)
        _chatMessages.value = _chatMessages.value + userMsg
        _isGeneratingAi.value = true

        viewModelScope.launch {
            val reply = geminiAssistant.queryAssistant(
                userPrompt = prompt,
                student = currentStudent.value,
                subjects = currentStudentGrades.value
            )
            val aiMsg = ChatMessage(text = reply, isFromUser = false)
            _chatMessages.value = _chatMessages.value + aiMsg
            _isGeneratingAi.value = false
        }
    }

    // CRUD: Intervention Create via DatabaseService
    fun createIntervention(
        studentRoll: String,
        studentName: String,
        dept: String,
        type: String,
        issue: String,
        actionPlan: String,
        faculty: String,
        deadline: String
    ) {
        viewModelScope.launch {
            val student = currentStudent.value
            val currentRisk = student?.riskScore ?: 70
            val targetRisk = (currentRisk - 25).coerceAtLeast(30)
            val currentAtt = student?.attendance ?: 68.0
            val targetAtt = (currentAtt + 10.0).coerceAtMost(95.0)

            val entity = InterventionEntity(
                studentRollNumber = studentRoll,
                studentName = studentName,
                department = dept,
                type = type,
                issue = issue,
                actionPlan = actionPlan,
                assignedFaculty = faculty,
                deadline = deadline,
                status = "IN_PROGRESS",
                beforeRisk = currentRisk,
                afterRisk = targetRisk,
                beforeAttendance = currentAtt,
                afterAttendance = targetAtt,
                isResolved = false
            )
            databaseService.createIntervention(entity)
        }
    }

    // CRUD: Intervention Update via DatabaseService
    fun markInterventionSuccessful(intervention: InterventionEntity) {
        viewModelScope.launch {
            databaseService.resolveIntervention(
                intervention = intervention,
                afterRisk = intervention.afterRisk,
                afterAttendance = intervention.afterAttendance
            )
        }
    }

    fun toggleStudyTask(taskId: Int, isCompleted: Boolean) {
        viewModelScope.launch {
            repository.toggleTaskCompleted(taskId, isCompleted)
        }
    }

    fun markAllNotificationsRead() {
        viewModelScope.launch {
            repository.markAllNotificationsAsRead()
        }
    }

    fun importStudent(
        name: String,
        roll: String,
        dept: String,
        att: Double,
        internal: Double,
        assign: Double,
        backlogs: Int
    ) {
        viewModelScope.launch {
            val risk = AiAnalyticsEngine.calculateRiskScore(
                attendance = att,
                internalMarks = internal,
                assignmentScore = assign,
                labScore = 65.0,
                quizScore = 60.0,
                previousGpa = 6.8,
                backlogs = backlogs,
                engagementScore = 60.0
            )
            val riskLevel = AiAnalyticsEngine.getRiskLevel(risk).name
            val successScore = AiAnalyticsEngine.calculateSuccessScore(att, internal, assign, 60.0, 60.0)
            val predictedGpa = AiAnalyticsEngine.predictSemesterGpa(6.8, internal, att)

            val student = StudentEntity(
                rollNumber = roll,
                name = name,
                department = dept,
                year = 3,
                semester = 5,
                section = "A",
                email = "${roll.lowercase()}@college.edu",
                phone = "+91 99887 76655",
                attendance = att,
                internalMarks = internal,
                assignmentScore = assign,
                labScore = 65.0,
                quizScore = 60.0,
                previousGpa = 6.8,
                currentGpa = 6.6,
                predictedGpa = predictedGpa,
                backlogs = backlogs,
                engagementScore = 60.0,
                riskScore = risk,
                riskLevel = riskLevel,
                successScore = successScore,
                cluster = if (risk > 60) "At Risk" else "Consistent Performers",
                advisorName = "Dr. S. K. Sharma"
            )
            repository.insertStudent(student)
        }
    }
}
