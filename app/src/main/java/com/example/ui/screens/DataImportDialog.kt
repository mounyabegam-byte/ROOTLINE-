package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.UploadFile
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.Primary
import com.example.ui.theme.Border
import com.example.ui.theme.Success

@Composable
fun DataImportDialog(
    onDismiss: () -> Unit,
    onImportSuccess: (name: String, roll: String, dept: String, att: Double, internal: Double, assign: Double, backlogs: Int) -> Unit
) {
    var rawCsvText by remember {
        mutableStateOf("21A91A0518,Devika Nair,CSE,66.0,52.0,58.0,2")
    }
    var isValidated by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("dialog_import_csv"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Border))
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.UploadFile, contentDescription = null, tint = Primary)
                    Spacer(modifier = Modifier.padding(horizontal = 8.dp))
                    Text(
                        text = "Import Records",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = "Format: Roll, Name, Dept, Attendance, Internal, Assignment, Backlogs",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.background, RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = rawCsvText,
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace,
                        color = Primary
                    )
                }

                OutlinedTextField(
                    value = rawCsvText,
                    onValueChange = {
                        rawCsvText = it
                        isValidated = false
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("CSV Input") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Primary,
                        unfocusedBorderColor = Border
                    ),
                    shape = RoundedCornerShape(8.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isValidated) "✓ Validated" else "Validation pending",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isValidated) Success else MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Button(
                        onClick = { isValidated = true },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer, contentColor = Primary),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(text = "Validate", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                    }
                }

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
                            val parts = rawCsvText.split(",")
                            if (parts.size >= 7) {
                                val roll = parts[0].trim()
                                val name = parts[1].trim()
                                val dept = parts[2].trim()
                                val att = parts[3].trim().toDoubleOrNull() ?: 70.0
                                val internal = parts[4].trim().toDoubleOrNull() ?: 60.0
                                val assign = parts[5].trim().toDoubleOrNull() ?: 65.0
                                val backlogs = parts[6].trim().toIntOrNull() ?: 0
                                onImportSuccess(name, roll, dept, att, internal, assign, backlogs)
                            }
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Primary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("btn_confirm_import_csv")
                    ) {
                        Text(text = "Ingest Data", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
