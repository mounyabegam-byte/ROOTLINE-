package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import com.example.ui.theme.Primary
import com.example.ui.theme.Border

@Composable
fun CreateInterventionDialog(
    initialRoll: String,
    initialName: String,
    initialDept: String,
    onDismiss: () -> Unit,
    onSubmit: (roll: String, name: String, dept: String, type: String, issue: String, action: String, faculty: String, deadline: String) -> Unit
) {
    var roll by remember { mutableStateOf(initialRoll) }
    var name by remember { mutableStateOf(initialName) }
    var dept by remember { mutableStateOf(initialDept) }
    var type by remember { mutableStateOf("Remedial Coaching") }
    var issue by remember { mutableStateOf("Low attendance and declining internal marks") }
    var action by remember { mutableStateOf("Weekly 1-on-1 tutoring session") }
    var faculty by remember { mutableStateOf("Prof. Ananya Kumar") }
    var deadline by remember { mutableStateOf("25 Oct 2026") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("dialog_create_intervention"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Border))
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Assign Intervention",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Establish an academic remedial roadmap",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(4.dp))

                DialogField("Student Roll", roll) { roll = it }
                DialogField("Student Name", name) { name = it }
                DialogField("Type", type) { type = it }
                DialogField("Observed Issue", issue) { issue = it }
                DialogField("Action Plan", action) { action = it }
                DialogField("Faculty In-charge", faculty) { faculty = it }
                DialogField("Deadline", deadline) { deadline = it }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Cancel",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.clickable { onDismiss() }.padding(12.dp)
                    )
                    
                    Spacer(modifier = Modifier.padding(horizontal = 8.dp))

                    Button(
                        onClick = {
                            onSubmit(roll, name, dept, type, issue, action, faculty, deadline)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Primary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("btn_submit_intervention")
                    ) {
                        Text(text = "Assign Plan", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun DialogField(label: String, value: String, onValueChange: (String) -> Unit) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Bold
        )
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
