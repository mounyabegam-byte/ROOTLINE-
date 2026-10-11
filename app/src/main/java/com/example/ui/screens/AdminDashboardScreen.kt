package com.example.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.data.local.entity.StudentEntity
import com.example.model.UserRole

@Composable
fun AdminDashboardScreen(
    students: List<StudentEntity>,
    currentRole: UserRole,
    onOpenImportDialog: () -> Unit,
    onRoleChange: (UserRole) -> Unit = {},
    onNavigateToAtRisk: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    when (currentRole) {
        UserRole.PRINCIPAL -> {
            PrincipalDashboardScreen(
                students = students,
                onOpenImportDialog = onOpenImportDialog,
                modifier = modifier
            )
        }
        UserRole.ADMIN -> {
            SuperAdminDashboardScreen(
                students = students,
                onOpenImportDialog = onOpenImportDialog,
                onRoleChange = onRoleChange,
                modifier = modifier
            )
        }
        else -> {
            HodAdminDashboardScreen(
                students = students,
                onOpenImportDialog = onOpenImportDialog,
                onNavigateToAtRisk = onNavigateToAtRisk,
                modifier = modifier
            )
        }
    }
}
