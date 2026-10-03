package com.example.ui.components

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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DieselOnPrimary
import com.example.ui.theme.DieselOnSurface
import com.example.ui.theme.DieselOnSurfaceVariant
import com.example.ui.theme.DieselPrimary
import com.example.ui.theme.DieselSurfaceContainerLow

@Composable
fun QuickRateEditDialog(
    initialRate: Double,
    dayNumber: Int,
    onDismiss: () -> Unit,
    onSaveRate: (Double) -> Unit
) {
    var rateText by remember { mutableStateOf(initialRate.toString()) }
    var isError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = "Update Day Fuel Rate (Col L)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = DieselOnSurface
                )
                Text(
                    text = "October 2026 • Day $dayNumber (Row #${dayNumber + 2})",
                    style = MaterialTheme.typography.bodySmall,
                    color = DieselOnSurfaceVariant
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Enter fuel price per liter in Indian Rupees (₹/L). This will update Column L for this date and automatically recalculate Column M (Total Cost).",
                    style = MaterialTheme.typography.bodyMedium,
                    color = DieselOnSurfaceVariant
                )

                OutlinedTextField(
                    value = rateText,
                    onValueChange = {
                        rateText = it
                        isError = it.toDoubleOrNull() == null || (it.toDoubleOrNull() ?: 0.0) <= 0.0
                    },
                    label = { Text("Fuel Rate (₹ / Liter)") },
                    prefix = {
                        Text("₹ ", fontWeight = FontWeight.Bold, color = DieselPrimary)
                    },
                    suffix = {
                        Text("INR/L", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    isError = isError,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("rate_input_field")
                )

                // Quick selector chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("92.50", "93.00", "94.20", "95.00").forEach { preset ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(DieselSurfaceContainerLow)
                                .clickable {
                                    rateText = preset
                                    isError = false
                                }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "₹$preset",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = DieselPrimary
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val rate = rateText.toDoubleOrNull()
                    if (rate != null && rate > 0) {
                        onSaveRate(rate)
                        onDismiss()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = DieselPrimary),
                modifier = Modifier.testTag("save_rate_confirm_button")
            ) {
                Text("Save Rate", color = DieselOnPrimary, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = DieselOnSurfaceVariant)
            }
        }
    )
}
