package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.ai.AiAnalyticsEngine
import com.example.data.local.entity.StudentEntity
import com.example.ui.components.MetricKpiCard
import com.example.ui.components.TopicGapCard
import com.example.ui.theme.Primary
import com.example.ui.theme.Success
import com.example.ui.theme.Warning
import com.example.ui.theme.Error
import com.example.ui.theme.Info
import com.example.ui.theme.Border
import com.example.ui.theme.Divider

@Composable
fun FacultyDashboardScreen(
    students: List<StudentEntity>,
    onUpdateMarks: (roll: String, code: String, internal: Double, assign: Double, lab: Double) -> Unit,
    onRecordAttendance: (roll: String, code: String, attended: Int, total: Int) -> Unit,
    onSelectStudent: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showMarksDialog by remember { mutableStateOf(false) }
    var selectedStudentForEdit by remember { mutableStateOf<StudentEntity?>(null) }

    val totalClassStudents = students.size
    val classAvgMarks = if (students.isNotEmpty()) students.map { it.internalMarks }.average().toInt() else 0
    val classAvgAttendance = if (students.isNotEmpty()) students.map { it.attendance }.average().toInt() else 0
    val weakCount = students.count { it.riskLevel == "HIGH" || it.internalMarks < 60 }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Column {
                Text(
                    text = "Faculty Portal",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Machine Learning (CS302) • Section A",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricKpiCard(
                    title = "Total Students",
                    value = "$totalClassStudents",
                    iconEmoji = "👥",
                    accentColor = Info,
                    trendText = "Class A",
                    isPositiveTrend = true,
                    modifier = Modifier.weight(1f)
                )

                MetricKpiCard(
                    title = "Class Avg",
                    value = "$classAvgMarks%",
                    iconEmoji = "📊",
                    accentColor = Primary,
                    trendText = "+5%",
                    isPositiveTrend = true,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricKpiCard(
                    title = "Attendance",
                    value = "$classAvgAttendance%",
                    iconEmoji = "⏰",
                    accentColor = Success,
                    trendText = "Stable",
                    isPositiveTrend = true,
                    modifier = Modifier.weight(1f)
                )

                MetricKpiCard(
                    title = "At Risk",
                    value = "$weakCount",
                    iconEmoji = "⚠️",
                    accentColor = Error,
                    trendText = "Review",
                    isPositiveTrend = false,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            TopicGapCard(topicGaps = AiAnalyticsEngine.sampleClassTopicGaps)
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Student Records",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Live Sync",
                    style = MaterialTheme.typography.labelSmall,
                    color = Primary,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        items(students) { student ->
            FacultyGradebookRow(
                student = student,
                onEditClick = {
                    selectedStudentForEdit = student
                    showMarksDialog = true
                },
                onViewProfile = { onSelectStudent(student.rollNumber) }
            )
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showMarksDialog && selectedStudentForEdit != null) {
        UpdateMarksDialog(
            student = selectedStudentForEdit!!,
            onDismiss = { showMarksDialog = false },
            onSave = { internal, assign, lab, attended, total ->
                onUpdateMarks(selectedStudentForEdit!!.rollNumber, "CS302", internal, assign, lab)
                onRecordAttendance(selectedStudentForEdit!!.rollNumber, "CS302", attended, total)
                showMarksDialog = false
            }
        )
    }
}

@Composable
private fun FacultyGradebookRow(
    student: StudentEntity,
    onEditClick: () -> Unit,
    onViewProfile: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("faculty_grade_row_${student.rollNumber}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Border))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f).clickable { onViewProfile() }) {
                Text(
                    text = student.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = student.rollNumber,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(text = "Mark: ${student.internalMarks.toInt()}%", style = MaterialTheme.typography.labelSmall, color = Primary)
                    Text(text = "Att: ${student.attendance.toInt()}%", style = MaterialTheme.typography.labelSmall, color = if (student.attendance < 75) Error else Success)
                }
            }

            Button(
                onClick = onEditClick,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer, contentColor = Primary),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.size(width = 90.dp, height = 36.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
            ) {
                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.size(4.dp))
                Text(text = "Edit", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun UpdateMarksDialog(
    student: StudentEntity,
    onDismiss: () -> Unit,
    onSave: (internal: Double, assign: Double, lab: Double, attended: Int, total: Int) -> Unit
) {
    var internalText by remember { mutableStateOf(student.internalMarks.toInt().toString()) }
    var assignText by remember { mutableStateOf(student.assignmentScore.toInt().toString()) }
    var labText by remember { mutableStateOf(student.labScore.toInt().toString()) }
    var attendedText by remember { mutableStateOf((student.attendance * 45 / 100).toInt().toString()) }
    var totalText by remember { mutableStateOf("45") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Border))
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Update Records",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = student.name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                EditGradeField("Internal Marks (%)", internalText) { internalText = it }
                EditGradeField("Assignment Score (%)", assignText) { assignText = it }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Column(modifier = Modifier.weight(1f)) {
                        EditGradeField("Attended", attendedText) { attendedText = it }
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        EditGradeField("Total", totalText) { totalText = it }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    Text(
                        text = "Cancel",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .clickable { onDismiss() }
                            .padding(12.dp)
                    )
                    Spacer(modifier = Modifier.size(12.dp))
                    Button(
                        onClick = {
                            val internal = internalText.toDoubleOrNull() ?: 0.0
                            val assign = assignText.toDoubleOrNull() ?: 0.0
                            val lab = labText.toDoubleOrNull() ?: 0.0
                            val attended = attendedText.toIntOrNull() ?: 0
                            val total = totalText.toIntOrNull() ?: 1
                            onSave(internal, assign, lab, attended, total)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Primary),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(text = "Save Changes", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun EditGradeField(label: String, value: String, onValueChange: (String) -> Unit) {
    Column {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Primary,
                unfocusedBorderColor = Border
            ),
            shape = RoundedCornerShape(8.dp),
            singleLine = true
        )
    }
}
