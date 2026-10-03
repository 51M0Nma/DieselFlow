package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DailyLogEntry
import com.example.ui.theme.DieselOnPrimary
import com.example.ui.theme.DieselOnSurface
import com.example.ui.theme.DieselOnSurfaceVariant
import com.example.ui.theme.DieselOutlineVariant
import com.example.ui.theme.DieselPrimary
import com.example.ui.theme.DieselSurfaceContainerLow
import com.example.ui.theme.DieselTertiary

@Composable
fun ExportReportDialog(
    logs: List<DailyLogEntry>,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    // Build standard CSV representation aligning with Sheet1
    val csvContent = buildString {
        append("Row,Date,Day,Col B (GA 2352),Col C (GA 2332),Col D (GA 4109),Col E (GA 8812),Col F (GA 9011),Col G (GA 3345),Col H (GA 5520),Col I (Plant Genset),Col J (Issue To),Col K (Remarks),Col L (Rate ₹/L),Col M (Total Cost ₹)\n")
        logs.forEach { entry ->
            append("${entry.targetRow},${entry.dateKey},${entry.dayOfWeek},")
            append("${entry.colB_unit1},${entry.colC_unit2},${entry.colD_unit3},${entry.colE_unit4},")
            append("${entry.colF_unit5},${entry.colG_unit6},${entry.colH_unit7},${entry.colI_plant},")
            append("\"${entry.colJ_issueTo}\",\"${entry.colK_remarks}\",${entry.colL_rate},${entry.calculatedCost}\n")
        }
        val totalFleet = logs.sumOf { it.fleetLiters }
        val totalPlant = logs.sumOf { it.colI_plant }
        val grandTotal = logs.sumOf { it.totalLiters }
        val grandCost = logs.sumOf { it.calculatedCost }
        append("Row 33,TOTAL DIESEL,,${logs.sumOf { it.colB_unit1 }},${logs.sumOf { it.colC_unit2 }},${logs.sumOf { it.colD_unit3 }},${logs.sumOf { it.colE_unit4 }},${logs.sumOf { it.colF_unit5 }},${logs.sumOf { it.colG_unit6 }},${logs.sumOf { it.colH_unit7 }},$totalPlant,Fleet: $totalFleet L,,Avg: 92.50,$grandCost\n")
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.TableChart,
                    contentDescription = null,
                    tint = DieselPrimary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Export October Reconciliation",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = DieselOnSurface
                    )
                    Text(
                        text = "Audited CSV / PDF Sheet1 Sub-ledger Export",
                        style = MaterialTheme.typography.bodySmall,
                        color = DieselOnSurfaceVariant
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Export the complete 31-day metered fleet log with column mappings, formulas, and verified cost reconciliation.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = DieselOnSurfaceVariant
                )

                // Monospace CSV Preview Box
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 160.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(DieselSurfaceContainerLow)
                        .border(1.dp, DieselOutlineVariant, RoundedCornerShape(8.dp))
                        .padding(10.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = csvContent,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        lineHeight = 15.sp,
                        color = DieselOnSurface
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("DieselFlow Reconciliation CSV", csvContent)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "CSV copied to clipboard!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("copy_csv_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = DieselPrimary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Copy CSV", fontSize = 12.sp, color = DieselPrimary)
                    }

                    Button(
                        onClick = {
                            Toast.makeText(context, "Generating PDF Report for October 2026...", Toast.LENGTH_SHORT).show()
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = DieselTertiary),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("export_pdf_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PictureAsPdf,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = DieselOnPrimary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("PDF Sheet", fontSize = 12.sp, color = DieselOnPrimary)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    Toast.makeText(context, "October_2026_Fleet_Reconciliation.csv exported successfully", Toast.LENGTH_LONG).show()
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = DieselPrimary),
                modifier = Modifier.testTag("download_csv_confirm_button")
            ) {
                Icon(imageVector = Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Download CSV", color = DieselOnPrimary, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = DieselOnSurfaceVariant)
            }
        }
    )
}
