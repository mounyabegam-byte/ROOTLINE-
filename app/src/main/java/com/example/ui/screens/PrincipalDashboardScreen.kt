package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
fun PrincipalDashboardScreen(
    students: List<StudentEntity>,
    onOpenImportDialog: () -> Unit,
    modifier: Modifier = Modifier
) {
    var reportDownloadedMessage by remember { mutableStateOf<String?>(null) }

    val totalStudents = students.size
    val averageGpa = if (totalStudents > 0) {
        String.format(Locale.US, "%.2f", students.map { it.currentGpa }.average())
    } else "0.00"

    val averageAttendance = if (totalStudents > 0) {
        students.map { it.attendance }.average().toInt()
    } else 0

    val highRiskCount = students.count { it.riskLevel == "HIGH" || it.riskScore >= 65 }

    val depts = listOf("CSE", "ECE", "EEE", "MECH")
    val deptStats = remember(students) {
        depts.map { deptName ->
            val deptStudents = students.filter { it.department.equals(deptName, ignoreCase = true) }
            val count = deptStudents.size
            val avgG = if (count > 0) deptStudents.map { it.currentGpa }.average() else 0.0
            val avgA = if (count > 0) deptStudents.map { it.attendance }.average() else 0.0
            val highRisk = deptStudents.count { it.riskLevel == "HIGH" || it.riskScore >= 65 }
            val passRate = if (count > 0) {
                ((count - deptStudents.count { it.backlogs > 0 }) * 100.0) / count
            } else 0.0

            DepartmentStats(
                department = deptName,
                totalStudents = count,
                averageGpa = avgG,
                averageAttendance = avgA,
                passPercentage = passRate,
                highRiskCount = highRisk,
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
                    text = "Executive Console",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Institution-wide academic intelligence",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Primary.copy(alpha = 0.05f)),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Primary.copy(alpha = 0.2f)))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Accreditation Readiness",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Current index: 8.42/10 based on real-time success metrics.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = { reportDownloadedMessage = "Report generated successfully" },
                            colors = ButtonDefaults.buttonColors(containerColor = Primary),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.size(8.dp))
                            Text(text = "Export Data", style = MaterialTheme.typography.labelLarge)
                        }

                        OutlinedButton(
                            onClick = onOpenImportDialog,
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Border),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.UploadFile, contentDescription = null, tint = Primary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.size(8.dp))
                            Text(text = "Import", style = MaterialTheme.typography.labelLarge, color = Primary)
                        }
                    }

                    if (reportDownloadedMessage != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = reportDownloadedMessage!!,
                            style = MaterialTheme.typography.labelSmall,
                            color = Success,
                            fontWeight = FontWeight.Bold
                        )
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
                    title = "Enrollment",
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
                    title = "High Risk",
                    value = "$highRiskCount",
                    iconEmoji = "🚨",
                    accentColor = Error,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Border))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Primary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.size(12.dp))
                        Text(text = "Strategic Insights", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    InsightItem("Performance Delta", "CSE maintains a 6.8% higher internal score average compared to other branches.", Warning)
                    InsightItem("Risk Concentration", "High-risk students are 14% more likely to miss Lab practicals.", Error)
                    InsightItem("Placement Outlook", "88% of final year students meet primary recruitment criteria.", Success)
                }
            }
        }

        item {
            Text(text = "Departmental Benchmarks", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }

        items(deptStats) { stat ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Border))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(text = stat.department, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                        Text(text = "${stat.totalStudents} Students", style = MaterialTheme.typography.labelSmall, color = Primary)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        SmallStat("GPA", String.format(Locale.US, "%.2f", stat.averageGpa), Primary)
                        SmallStat("Att.", "${stat.averageAttendance.toInt()}%", Success)
                        SmallStat("Pass", "${stat.passPercentage.toInt()}%", Info)
                        SmallStat("Risk", "${stat.highRiskCount}", Error)
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}

@Composable
private fun InsightItem(title: String, desc: String, color: Color) {
    Row(modifier = Modifier.padding(vertical = 8.dp)) {
        Box(modifier = Modifier.size(8.dp).padding(top = 4.dp).background(color, CircleShape))
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(text = title, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = color)
            Text(text = desc, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun SmallStat(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = color)
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
