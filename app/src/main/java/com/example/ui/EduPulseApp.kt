package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.auth.ProvideAuthContext
import com.example.model.UserRole
import com.example.ui.auth.AuthViewModel
import com.example.ui.screens.AdminDashboardScreen
import com.example.ui.screens.AiAssistantScreen
import com.example.ui.screens.AtRiskStudentsScreen
import com.example.ui.screens.AttendanceManagementScreen
import com.example.ui.screens.CreateInterventionDialog
import com.example.ui.screens.DataImportDialog
import com.example.ui.screens.FacultyDashboardScreen
import com.example.ui.screens.InterventionsScreen
import com.example.ui.screens.NotificationsScreen
import com.example.ui.screens.StaffProfileScreen
import com.example.ui.screens.StudentAttendanceScreen
import com.example.ui.screens.StudentDashboardScreen
import com.example.ui.screens.StudentPerformanceScreen
import com.example.ui.screens.StudentProfileScreen
import com.example.ui.screens.StudyPlanScreen
import com.example.ui.screens.auth.ForgotPasswordDialog
import com.example.ui.screens.auth.LoginScreen
import com.example.ui.screens.auth.SignupScreen
import com.example.ui.theme.Primary
import com.example.ui.theme.Error
import com.example.ui.theme.Background
import com.example.ui.theme.Surface
import com.example.ui.theme.Border

enum class NavDestination(val label: String, val icon: ImageVector) {
    DASHBOARD("Home", Icons.Default.Dashboard),
    PERFORMANCE("Grades", Icons.Default.BarChart),
    ATTENDANCE("Attendance", Icons.Default.DateRange),
    AT_RISK("Risks", Icons.Default.Warning),
    INTERVENTIONS("Actions", Icons.Default.Handshake),
    STUDY_PLAN("Planner", Icons.Default.CalendarMonth),
    AI_ASSISTANT("Advisor", Icons.Default.AutoAwesome),
    PROFILE("Account", Icons.Default.Person),
    NOTIFICATIONS("Alerts", Icons.Default.Notifications)
}

@Composable
fun EduPulseApp(
    viewModel: MainViewModel,
    authViewModel: AuthViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    ProvideAuthContext(authContext = authViewModel) {
        val currentUser by authViewModel.currentUser.collectAsState()
        val currentRole by authViewModel.currentRole.collectAsState()
        val isLoadingAuth by authViewModel.isLoading.collectAsState()
        val authError by authViewModel.authError.collectAsState()

        androidx.compose.runtime.LaunchedEffect(currentUser) {
            currentUser?.rollNumber?.let { roll ->
                viewModel.selectStudent(roll)
            }
        }

        var authScreenMode by remember { mutableStateOf("LOGIN") }
        var showForgotPasswordDialog by remember { mutableStateOf(false) }
        var forgotPasswordEmail by remember { mutableStateOf("") }

        if (currentUser == null) {
            if (authScreenMode == "LOGIN") {
                LoginScreen(
                    isLoading = isLoadingAuth,
                    errorMessage = authError,
                    onLoginSubmit = { email, pass -> authViewModel.login(email, pass) },
                    onNavigateToSignup = { authScreenMode = "SIGNUP" },
                    onForgotPasswordClick = { email ->
                        forgotPasswordEmail = email
                        showForgotPasswordDialog = true
                    }
                )
            } else {
                SignupScreen(
                    isLoading = isLoadingAuth,
                    errorMessage = authError,
                    onSignupSubmit = { name, email, pass, role, dept, roll ->
                        authViewModel.signup(name, email, pass, role, dept, roll)
                    },
                    onNavigateToLogin = { authScreenMode = "LOGIN" }
                )
            }

            if (showForgotPasswordDialog) {
                ForgotPasswordDialog(
                    initialEmail = forgotPasswordEmail,
                    onDismiss = { showForgotPasswordDialog = false },
                    onSubmit = { email ->
                        authViewModel.sendPasswordReset(email)
                    }
                )
            }
            return@ProvideAuthContext
        }

        val currentStudent by viewModel.currentStudent.collectAsState()
        val allStudents by viewModel.filteredStudents.collectAsState()
        val grades by viewModel.currentStudentGrades.collectAsState()
        val badges by viewModel.currentStudentBadges.collectAsState()
        val tasks by viewModel.currentStudentTasks.collectAsState()
        val interventions by viewModel.allInterventions.collectAsState()
        val notifications by viewModel.allNotifications.collectAsState()
        val chatMessages by viewModel.chatMessages.collectAsState()
        val isGeneratingAi by viewModel.isGeneratingAi.collectAsState()

        var currentNav by remember { mutableStateOf(NavDestination.DASHBOARD) }
        var showCreateInterventionDialog by remember { mutableStateOf(false) }
        var showImportDialog by remember { mutableStateOf(false) }

        val unreadNotifications = notifications.count { !it.isRead }

        BackHandler(enabled = currentNav != NavDestination.DASHBOARD) {
            currentNav = NavDestination.DASHBOARD
        }

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                TopAppBarHeader(
                    currentRole = currentRole,
                    userName = currentUser?.displayName ?: "User",
                    onNotificationsClick = { currentNav = NavDestination.NOTIFICATIONS },
                    onLogoutClick = {
                        authViewModel.logout()
                        viewModel.logout()
                    },
                    unreadCount = unreadNotifications
                )
            },
            bottomBar = {
                ModernBottomNavBar(
                    currentNav = currentNav,
                    currentRole = currentRole,
                    onSelectNav = { currentNav = it }
                )
            },
            containerColor = Background
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (currentNav) {
                    NavDestination.DASHBOARD -> {
                        when (currentRole) {
                            UserRole.STUDENT, UserRole.MENTOR -> {
                                StudentDashboardScreen(
                                    student = currentStudent,
                                    subjects = grades,
                                    badges = badges,
                                    onNavigateToChat = { currentNav = NavDestination.AI_ASSISTANT },
                                    onNavigateToStudyPlan = { currentNav = NavDestination.STUDY_PLAN },
                                    onNavigateToProfile = { currentNav = NavDestination.PROFILE }
                                )
                            }
                            UserRole.FACULTY, UserRole.STAFF -> {
                                FacultyDashboardScreen(
                                    students = allStudents,
                                    onUpdateMarks = { roll, code, internal, assign, lab ->
                                        viewModel.updateStudentMarks(roll, code, internal, assign, lab)
                                    },
                                    onRecordAttendance = { roll, code, attended, total ->
                                        viewModel.recordStudentAttendance(roll, code, attended, total)
                                    },
                                    onSelectStudent = { roll ->
                                        viewModel.selectStudent(roll)
                                        currentNav = NavDestination.PROFILE
                                    }
                                )
                            }
                            UserRole.HOD, UserRole.PRINCIPAL, UserRole.ADMIN -> {
                                AdminDashboardScreen(
                                    students = allStudents,
                                    currentRole = currentRole,
                                    onOpenImportDialog = { showImportDialog = true },
                                    onRoleChange = { newRole -> authViewModel.switchRole(newRole) },
                                    onNavigateToAtRisk = { currentNav = NavDestination.AT_RISK }
                                )
                            }
                        }
                    }

                    NavDestination.PERFORMANCE -> {
                        StudentPerformanceScreen(
                            student = currentStudent,
                            subjects = grades
                        )
                    }

                    NavDestination.ATTENDANCE -> {
                        if (currentRole == UserRole.STUDENT || currentRole == UserRole.MENTOR) {
                            StudentAttendanceScreen(
                                student = currentStudent,
                                subjects = grades
                            )
                        } else {
                            AttendanceManagementScreen(
                                students = allStudents,
                                onRecordAttendance = { roll, code, attended, total ->
                                    viewModel.recordStudentAttendance(roll, code, attended, total)
                                }
                            )
                        }
                    }

                    NavDestination.AT_RISK -> {
                        AtRiskStudentsScreen(
                            students = allStudents,
                            onSelectStudent = { roll ->
                                viewModel.selectStudent(roll)
                                currentNav = NavDestination.PROFILE
                            },
                            onCreateInterventionForStudent = { student ->
                                viewModel.selectStudent(student.rollNumber)
                                showCreateInterventionDialog = true
                            }
                        )
                    }

                    NavDestination.AI_ASSISTANT -> {
                        AiAssistantScreen(
                            messages = chatMessages,
                            isGenerating = isGeneratingAi,
                            onSendMessage = { viewModel.sendChatMessage(it) },
                            studentName = currentStudent?.name
                        )
                    }

                    NavDestination.INTERVENTIONS -> {
                        InterventionsScreen(
                            interventions = interventions,
                            onCreateInterventionClick = { showCreateInterventionDialog = true },
                            onMarkSuccessful = { viewModel.markInterventionSuccessful(it) }
                        )
                    }

                    NavDestination.STUDY_PLAN -> {
                        StudyPlanScreen(
                            tasks = tasks,
                            onToggleTask = { id, done -> viewModel.toggleStudyTask(id, done) },
                            studentName = currentStudent?.name
                        )
                    }

                    NavDestination.PROFILE -> {
                        if (currentRole == UserRole.STUDENT) {
                            StudentProfileScreen(
                                student = currentStudent,
                                subjects = grades,
                                badges = badges,
                                onLogout = {
                                    authViewModel.logout()
                                    viewModel.logout()
                                }
                            )
                        } else {
                            StaffProfileScreen(
                                currentUser = currentUser!!,
                                onLogout = {
                                    authViewModel.logout()
                                    viewModel.logout()
                                }
                            )
                        }
                    }

                    NavDestination.NOTIFICATIONS -> {
                        NotificationsScreen(
                            notifications = notifications,
                            onMarkAllRead = { viewModel.markAllNotificationsRead() }
                        )
                    }
                }

                if (showCreateInterventionDialog) {
                    CreateInterventionDialog(
                        initialRoll = currentStudent?.rollNumber ?: "",
                        initialName = currentStudent?.name ?: "",
                        initialDept = currentStudent?.department ?: "",
                        onDismiss = { showCreateInterventionDialog = false },
                        onSubmit = { roll, name, dept, type, issue, action, faculty, deadline ->
                            viewModel.createIntervention(roll, name, dept, type, issue, action, faculty, deadline)
                        }
                    )
                }

                if (showImportDialog) {
                    DataImportDialog(
                        onDismiss = { showImportDialog = false },
                        onImportSuccess = { name, roll, dept, att, internal, assign, backlogs ->
                            viewModel.importStudent(name, roll, dept, att, internal, assign, backlogs)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun TopAppBarHeader(
    currentRole: UserRole,
    userName: String,
    onNotificationsClick: () -> Unit,
    onLogoutClick: () -> Unit,
    unreadCount: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.statusBars)
            .background(Surface)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "EduPulse AI",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Primary
            )
            Text(
                text = "$userName • ${currentRole.label}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = onNotificationsClick,
                modifier = Modifier.size(40.dp)
            ) {
                BadgedBox(
                    badge = {
                        if (unreadCount > 0) {
                            Badge(containerColor = Error) {
                                Text(text = "$unreadCount", color = Color.White, style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Notifications",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.size(8.dp))

            IconButton(
                onClick = onLogoutClick,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Logout,
                    contentDescription = "Sign Out",
                    tint = Error.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
private fun ModernBottomNavBar(
    currentNav: NavDestination,
    currentRole: UserRole,
    onSelectNav: (NavDestination) -> Unit
) {
    val items = when (currentRole) {
        UserRole.STUDENT, UserRole.MENTOR -> listOf(
            NavDestination.DASHBOARD,
            NavDestination.PERFORMANCE,
            NavDestination.ATTENDANCE,
            NavDestination.STUDY_PLAN,
            NavDestination.PROFILE
        )
        UserRole.FACULTY, UserRole.STAFF -> listOf(
            NavDestination.DASHBOARD,
            NavDestination.ATTENDANCE,
            NavDestination.AT_RISK,
            NavDestination.INTERVENTIONS,
            NavDestination.PROFILE
        )
        UserRole.HOD, UserRole.PRINCIPAL, UserRole.ADMIN -> listOf(
            NavDestination.DASHBOARD,
            NavDestination.AT_RISK,
            NavDestination.INTERVENTIONS,
            NavDestination.AI_ASSISTANT,
            NavDestination.PROFILE
        )
    }

    NavigationBar(
        modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars),
        containerColor = Surface,
        tonalElevation = 0.dp
    ) {
        items.forEach { dest ->
            val isSelected = currentNav == dest
            NavigationBarItem(
                selected = isSelected,
                onClick = { onSelectNav(dest) },
                icon = {
                    Icon(
                        imageVector = dest.icon,
                        contentDescription = dest.label,
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = {
                    Text(
                        text = dest.label,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Primary,
                    selectedTextColor = Primary,
                    indicatorColor = Primary.copy(alpha = 0.1f),
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}
