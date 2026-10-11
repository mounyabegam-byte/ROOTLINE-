package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.Warning
import com.example.ui.theme.Success
import com.example.ui.theme.Error

@Composable
fun RiskBadge(
    riskLevel: String,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor, label) = when (riskLevel.uppercase()) {
        "HIGH" -> Triple(Error.copy(alpha = 0.1f), Error, "High Risk")
        "MEDIUM" -> Triple(Warning.copy(alpha = 0.1f), Warning, "Medium Risk")
        else -> Triple(Success.copy(alpha = 0.1f), Success, "Low Risk")
    }

    Box(
        modifier = modifier
            .testTag("risk_badge_${riskLevel.lowercase()}")
            .background(bgColor, RoundedCornerShape(6.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = label.uppercase(),
            color = textColor,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )
    }
}
