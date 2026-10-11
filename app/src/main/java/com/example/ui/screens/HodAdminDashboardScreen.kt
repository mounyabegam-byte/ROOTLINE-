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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.StudentEntity
import com.example.model.DepartmentStats
import com.example.ui.components.MetricKpiCard
import com.example.ui.theme.Primary
import com.example.ui.theme.Success
import com.example.ui.theme.Warning
import com.example.ui.theme.Error
import com.example.ui.theme.Info
import com.example.ui.theme.Border
import com.example.ui.theme.Divider
import java.util.Locale

@Composable
fun HodAdminDashboardScreen(
    students: List<StudentEntity>,
    onOpenImportDialog: () -> Unit,
    onNavigateToAtRisk: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val totalStudents = students.size
    val averageGpa = if (totalStudents > 0) {
        String.format(Locale.US, "%.2f", students.map { it.currentGpa }.average())
    } else "0.00"

    val averageAttendance = if (totalStudents > 0) {
        students.map { it.attendance }.average().toInt()
    } else 0

    val passStudentsCount = students.count { it.backlogs == 0 }
    val passPercentage = if (totalStudents > 0) {
        (passStudentsCount * 100) / totalStudents
    } else 0

    val highRiskCount = students.count { it.riskLevel == "HIGH" || it.riskScore >= 65 }

    val depts = listOf("CSE", "ECE", "EEE", "MECH")
    val deptStats = androidx.compose.runtime.remember(students) {
        depts.map { deptName ->
            val deptStudents = students.filter { it.department.equals(deptName, ignoreCase = true) }
            val count = deptStudents.size
            val avgG = if (count > 0) deptStudents.map { it.currentGpa }.average() else 0.0
            val avgA = if (count > 0) deptStudents.map { it.attendance }.average() else 0.0
            val highR = deptStudents.count { it.riskLevel == "HIGH" || it.riskScore >= 65 }
            val passRate = if (count > 0) {
                ((count - deptStudents.count { it.backlogs > 0 }) * 100.0) / count
            } else 0.0

            DepartmentStats(
                department = deptName,
                totalStudents = count,
                averageGpa = avgG,
                averageAttendance = avgA,
                passPercentage = passRate,
                highRiskCount = highR,
                backlogsCount = deptStudents.sumOf { it.backlogs }
            )
        }
    }

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
                    text = "Analytics Overview",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Departmental trends and predictive cohorts",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Primary.copy(alpha = 0.2f)))
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Data Ingestion Pipeline", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                        Text(text = "Import latest academic records", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Button(
                        onClick = onOpenImportDialog,
                        colors = ButtonDefaults.buttonColors(containerColor = Primary),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.size(8.dp))
                        Text(text = "Import CSV", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricKpiCard(
                    title = "Total Students",
                    value = "$totalStudents",
                    iconEmoji = "🏛️",
                    accentColor = Info,
                    modifier = Modifier.weight(1f)
                )
                MetricKpiCard(
                    title = "Avg. GPA",
                    value = averageGpa,
                    iconEmoji = "📈",
                    accentColor = Primary,
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
                    value = "$averageAttendance%",
                    iconEmoji = "⏰",
                    accentColor = Success,
                    modifier = Modifier.weight(1f)
                )
                MetricKpiCard(
                    title = "Pass Rate",
                    value = "$passPercentage%",
                    iconEmoji = "🎓",
                    accentColor = Warning,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            RiskDistributionCard(students = students, onNavigateToAtRisk = onNavigateToAtRisk)
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Border))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(text = "Department Comparison", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))
                    deptStats.forEach { stat ->
                        Column(modifier = Modifier.padding(vertical = 8.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(text = stat.department, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                Text(text = "${stat.totalStudents} Students", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(text = "GPA: ${String.format(Locale.US, "%.2f", stat.averageGpa)}", style = MaterialTheme.typography.labelSmall, color = Primary)
                                Text(text = "Att: ${stat.averageAttendance.toInt()}%", style = MaterialTheme.typography.labelSmall, color = Success)
                                Text(text = "Risk: ${stat.highRiskCount}", style = MaterialTheme.typography.labelSmall, color = Error)
                            }
                            if (stat != deptStats.last()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Divider))
                            }
                        }
                    }
                }
            }
        }

        item {
            StudentClusteringCard()
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}

@Composable
private fun RiskDistributionCard(
    students: List<StudentEntity>,
    onNavigateToAtRisk: (() -> Unit)? = null
) {
    val total = students.size.coerceAtLeast(1)
    val highRiskCount = students.count { it.riskLevel == "HIGH" || it.riskScore >= 65 }
    val mediumRiskCount = students.count { it.riskLevel == "MEDIUM" || (it.riskScore in 45..64) }
    val lowRiskCount = students.count { it.riskLevel == "LOW" || it.riskScore < 45 }

    Card(
        modifier = Modifier.fillMaxWidth().clickable { onNavigateToAtRisk?.invoke() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Border))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(text = "Risk Distribution", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                if (onNavigateToAtRisk != null) {
                    Text(text = "View Detailed →", style = MaterialTheme.typography.labelSmall, color = Primary, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            DistributionRow("Low Risk", lowRiskCount.toFloat() / total, Success)
            Spacer(modifier = Modifier.height(12.dp))
            DistributionRow("Medium Risk", mediumRiskCount.toFloat() / total, Warning)
            Spacer(modifier = Modifier.height(12.dp))
            DistributionRow("High Risk", highRiskCount.toFloat() / total, Error)
        }
    }
}

@Composable
private fun DistributionRow(label: String, weight: Float, color: Color) {
    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = label, style = MaterialTheme.typography.labelMedium)
            Text(text = "${(weight * 100).toInt()}%", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = color)
        }
        Spacer(modifier = Modifier.height(6.dp))
        LinearProgressIndicator(
            progress = { weight },
            modifier = Modifier.fillMaxWidth().height(8.dp),
            color = color,
            trackColor = Divider,
            strokeCap = StrokeCap.Round
        )
    }
}

@Composable
private fun StudentClusteringCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Border))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Primary, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Text(text = "AI Behavioral Cohorts", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(16.dp))
            
            ClusterItem("High Performers", "Students with >8.5 GPA and consistent attendance.", Success)
            ClusterItem("Developing", "Moderate scores with potential for GPA growth.", Info)
            ClusterItem("Support Required", "Flags detected in attendance and internal marks.", Error)
        }
    }
}

@Composable
private fun ClusterItem(title: String, desc: String, color: Color) {
    Row(modifier = Modifier.padding(vertical = 8.dp)) {
        Box(modifier = Modifier.size(8.dp).padding(top = 4.dp).background(color, CircleShape))
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(text = title, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
            Text(text = desc, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
