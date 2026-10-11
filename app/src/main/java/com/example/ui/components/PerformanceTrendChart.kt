package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.Border
import com.example.ui.theme.Primary
import com.example.ui.theme.Success
import com.example.ui.theme.Info
import com.example.ui.theme.Divider

@Composable
fun PerformanceTrendChart(
    semesterHistory: List<Pair<String, Double>>,
    predictedGpa: Double,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("performance_trend_chart"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Border)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "GPA Trend",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Historical vs AI Projected",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    LegendItem("Historical", Info)
                    LegendItem("Forecast", Success)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Canvas Chart Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
            ) {
                Canvas(modifier = Modifier.matchParentSize()) {
                    val w = size.width
                    val h = size.height
                    val bottomPadding = 24.dp.toPx()
                    val chartHeight = h - bottomPadding

                    // Grid Lines
                    val gpaSteps = listOf(10.0, 8.0, 6.0, 4.0)
                    gpaSteps.forEach { gpaVal ->
                        val y = (1.0 - (gpaVal - 4.0) / 6.0).toFloat() * chartHeight
                        drawLine(
                            color = Divider,
                            start = Offset(0f, y),
                            end = Offset(w, y),
                            strokeWidth = 1.dp.toPx()
                        )
                    }

                    // Data Points
                    val allPoints = semesterHistory + ("Predicted" to predictedGpa)
                    val pointOffsets = mutableListOf<Offset>()
                    val stepX = w / (allPoints.size - 1)

                    allPoints.forEachIndexed { i, pair ->
                        val x = i * stepX
                        val normalizedY = ((1.0 - ((pair.second - 4.0) / 6.0).coerceIn(0.0, 1.0)) * chartHeight).toFloat()
                        pointOffsets.add(Offset(x, normalizedY))
                    }

                    // Gradient Fill for History
                    val pathFill = Path().apply {
                        moveTo(pointOffsets.first().x, pointOffsets.first().y)
                        for (i in 1 until pointOffsets.size) {
                            lineTo(pointOffsets[i].x, pointOffsets[i].y)
                        }
                        lineTo(pointOffsets.last().x, chartHeight)
                        lineTo(pointOffsets.first().x, chartHeight)
                        close()
                    }

                    drawPath(
                        path = pathFill,
                        brush = Brush.verticalGradient(
                            listOf(Info.copy(alpha = 0.1f), Color.Transparent),
                            startY = 0f,
                            endY = chartHeight
                        )
                    )

                    // Historical Line
                    val historyPath = Path().apply {
                        moveTo(pointOffsets.first().x, pointOffsets.first().y)
                        for (i in 1 until pointOffsets.size - 1) {
                            lineTo(pointOffsets[i].x, pointOffsets[i].y)
                        }
                    }
                    drawPath(
                        path = historyPath,
                        color = Info,
                        style = Stroke(width = 2.dp.toPx())
                    )

                    // Forecast Line (Dashed)
                    if (pointOffsets.size >= 2) {
                        val lastReal = pointOffsets[pointOffsets.size - 2]
                        val predicted = pointOffsets.last()
                        drawLine(
                            color = Success,
                            start = lastReal,
                            end = predicted,
                            strokeWidth = 2.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                        )
                    }

                    // Dots
                    pointOffsets.forEachIndexed { i, offset ->
                        val isPredicted = i == pointOffsets.size - 1
                        drawCircle(
                            color = if (isPredicted) Success else Info,
                            radius = 4.dp.toPx(),
                            center = offset
                        )
                        drawCircle(
                            color = Color.White,
                            radius = 2.dp.toPx(),
                            center = offset
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // X-Axis Labels
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val labels = semesterHistory.map { it.first } + "Target"
                labels.forEach { label ->
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (label == "Target") Success else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = if (label == "Target") FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
    }
}

@Composable
private fun LegendItem(text: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(color, CircleShape)
        )
        Spacer(modifier = Modifier.size(6.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
