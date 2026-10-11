package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.StudentEntity
import com.example.ui.theme.Primary
import com.example.ui.theme.Success
import com.example.ui.theme.Warning
import com.example.ui.theme.Error
import com.example.ui.theme.Info
import com.example.ui.theme.Border
import com.example.ui.theme.Divider

@Composable
fun AtRiskStudentsScreen(
    students: List<StudentEntity>,
    onSelectStudent: (String) -> Unit,
    onCreateInterventionForStudent: (StudentEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedRiskFilter by remember { mutableStateOf("ALL") }
    var selectedDeptFilter by remember { mutableStateOf("ALL") }

    val atRiskStudents = remember(students, searchQuery, selectedRiskFilter, selectedDeptFilter) {
        students.filter { student ->
            val isAtRiskCohort = student.riskLevel == "HIGH" || student.riskLevel == "MEDIUM" || student.riskScore >= 45
            val matchesRisk = when (selectedRiskFilter) {
                "HIGH" -> student.riskLevel == "HIGH" || student.riskScore >= 65
                "MEDIUM" -> student.riskLevel == "MEDIUM" || (student.riskScore in 45..64)
                else -> isAtRiskCohort
            }
            val matchesDept = selectedDeptFilter == "ALL" || student.department.equals(selectedDeptFilter, ignoreCase = true)
            val matchesSearch = searchQuery.isBlank() ||
                    student.name.contains(searchQuery, ignoreCase = true) ||
                    student.rollNumber.contains(searchQuery, ignoreCase = true)

            matchesRisk && matchesDept && matchesSearch
        }.sortedByDescending { it.riskScore }
    }

    val highRiskCount = students.count { it.riskLevel == "HIGH" || it.riskScore >= 65 }
    val mediumRiskCount = students.count { it.riskLevel == "MEDIUM" || (it.riskScore in 45..64) }

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
                    text = "Risk Monitoring",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Early warning indicators for academic success",
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
                SummaryKpiCard("High Risk", "$highRiskCount", Error, Modifier.weight(1f))
                SummaryKpiCard("Medium Risk", "$mediumRiskCount", Warning, Modifier.weight(1f))
                SummaryKpiCard("Total Flagged", "${highRiskCount + mediumRiskCount}", Info, Modifier.weight(1f))
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Border))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Search student name or roll...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Primary,
                            unfocusedBorderColor = Border
                        ),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(text = "Filter by Risk", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        item { RiskFilterChip("All", selectedRiskFilter == "ALL", { selectedRiskFilter = "ALL" }, Primary) }
                        item { RiskFilterChip("High", selectedRiskFilter == "HIGH", { selectedRiskFilter = "HIGH" }, Error) }
                        item { RiskFilterChip("Medium", selectedRiskFilter == "MEDIUM", { selectedRiskFilter = "MEDIUM" }, Warning) }
                    }
                }
            }
        }

        items(atRiskStudents, key = { it.rollNumber }) { student ->
            AtRiskStudentCard(
                student = student,
                onSelect = { onSelectStudent(student.rollNumber) },
                onCreateIntervention = { onCreateInterventionForStudent(student) }
            )
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}

@Composable
private fun SummaryKpiCard(title: String, value: String, color: Color, modifier: Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Border))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = color)
            Text(text = title, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun RiskFilterChip(label: String, isSelected: Boolean, onClick: () -> Unit, color: Color) {
    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = { Text(text = label, style = MaterialTheme.typography.labelMedium) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = color.copy(alpha = 0.1f),
            selectedLabelColor = color
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = isSelected,
            borderColor = if (isSelected) color else Border
        ),
        shape = RoundedCornerShape(8.dp)
    )
}

@Composable
private fun AtRiskStudentCard(student: StudentEntity, onSelect: () -> Unit, onCreateIntervention: () -> Unit) {
    val riskColor = if (student.riskScore >= 65) Error else Warning

    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onSelect),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Border))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = student.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                    Text(text = student.rollNumber, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Box(
                    modifier = Modifier
                        .background(riskColor.copy(alpha = 0.1f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(text = "${student.riskScore}% Risk", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = riskColor)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = { student.riskScore / 100f },
                modifier = Modifier.fillMaxWidth().height(6.dp),
                color = riskColor,
                trackColor = Divider,
                strokeCap = StrokeCap.Round
            )

            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text(text = "${student.attendance.toInt()}%", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = if (student.attendance < 75) Error else Success)
                    Text(text = "Attendance", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Column {
                    Text(text = student.predictedGpa.toString(), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                    Text(text = "Est. GPA", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Button(
                    onClick = onCreateIntervention,
                    modifier = Modifier.height(36.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                    contentPadding = PaddingValues(horizontal = 12.dp)
                ) {
                    Icon(Icons.Default.Handshake, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Intervene", style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}
