package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ai.AiAnalyticsEngine
import com.example.data.local.entity.BadgeEntity
import com.example.data.local.entity.StudentEntity
import com.example.data.local.entity.SubjectGradeEntity
import com.example.ui.components.AiRecommendationCard
import com.example.ui.components.ExplainableAiCard
import com.example.ui.components.MetricKpiCard
import com.example.ui.components.PerformanceTrendChart
import com.example.ui.components.RiskBadge
import com.example.ui.components.SubjectBarChart
import com.example.ui.components.SuccessScoreGauge
import com.example.ui.theme.Border
import com.example.ui.theme.Primary
import com.example.ui.theme.Secondary
import com.example.ui.theme.Success
import com.example.ui.theme.Warning
import com.example.ui.theme.Info
import com.example.ui.theme.Divider

@Composable
fun StudentDashboardScreen(
    student: StudentEntity?,
    subjects: List<SubjectGradeEntity>,
    badges: List<BadgeEntity>,
    onNavigateToChat: () -> Unit,
    onNavigateToStudyPlan: () -> Unit,
    onNavigateToProfile: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (student == null) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = "Loading analytics...", style = MaterialTheme.typography.bodyMedium)
        }
        return
    }

    val history = listOf(
        "Sem 1" to 6.0,
        "Sem 2" to 6.3,
        "Sem 3" to student.previousGpa,
        "Current" to student.currentGpa
    )

    val weakSubjects = subjects.filter { it.overallScore < 70 || it.aiStatus == "NEEDS_ATTENTION" }
    val recommendations = AiAnalyticsEngine.generateRecommendations(student, weakSubjects)
    val factors = AiAnalyticsEngine.generateExplainableFactors(student)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(16.dp))
            // Profile Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Hello, ${student.name.split(" ").first()}",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${student.department} • Year ${student.year}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                RiskBadge(riskLevel = student.riskLevel)
            }
        }

        // KPI Grid
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricKpiCard(
                    title = "Academic Score",
                    value = "${student.internalMarks.toInt()}%",
                    iconEmoji = "📊",
                    accentColor = Primary,
                    trendText = "+4%",
                    isPositiveTrend = true,
                    modifier = Modifier.weight(1f)
                )

                MetricKpiCard(
                    title = "Attendance",
                    value = "${student.attendance.toInt()}%",
                    iconEmoji = "⏰",
                    accentColor = if (student.attendance >= 75.0) Success else Warning,
                    trendText = if (student.attendance >= 75.0) "Optimal" else "Low",
                    isPositiveTrend = student.attendance >= 75.0,
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
                    title = "Assignments",
                    value = "${student.assignmentScore.toInt()}%",
                    iconEmoji = "📝",
                    accentColor = Info,
                    trendText = "91% rate",
                    isPositiveTrend = true,
                    modifier = Modifier.weight(1f)
                )

                MetricKpiCard(
                    title = "Success Index",
                    value = "${student.successScore}",
                    iconEmoji = "🎯",
                    accentColor = Secondary,
                    trendText = "Stable",
                    isPositiveTrend = true,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // AI Assistant Call-to-action
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToChat() }
                    .testTag("ai_assistant_banner_card"),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Primary),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(Color.White.copy(alpha = 0.2f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.size(16.dp))
                        Column {
                            Text(
                                text = "EduPulse AI Assistant",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Ask about your performance or study plan",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                    }

                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = Color.White
                    )
                }
            }
        }

        item {
            SuccessScoreGauge(
                score = student.successScore,
                attendance = student.attendance,
                internalMarks = student.internalMarks,
                assignmentScore = student.assignmentScore
            )
        }

        item {
            PerformanceTrendChart(
                semesterHistory = history,
                predictedGpa = student.predictedGpa
            )
        }

        item {
            SubjectBarChart(subjects = subjects)
        }

        item {
            ExplainableAiCard(
                riskScore = student.riskScore,
                factors = factors
            )
        }

        item {
            AiRecommendationCard(recommendations = recommendations)
        }

        // Study Plan Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToStudyPlan() },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Border))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Personalized Study Plan",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Tailored focus for this week",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Button(
                        onClick = onNavigateToStudyPlan,
                        colors = ButtonDefaults.buttonColors(containerColor = Primary),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(text = "View Plan", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }

        item {
            BadgesSummarySection(badges = badges)
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun BadgesSummarySection(badges: List<BadgeEntity>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
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
                Text(
                    text = "Achievements",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${badges.count { it.isUnlocked }}/${badges.size} Unlocked",
                    style = MaterialTheme.typography.labelSmall,
                    color = Primary,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                badges.take(4).forEach { badge ->
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .background(
                                if (badge.isUnlocked) Primary.copy(alpha = 0.05f) else Divider,
                                RoundedCornerShape(8.dp)
                            )
                            .padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = badge.iconEmoji, fontSize = 20.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = badge.title,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = if (badge.isUnlocked) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}
