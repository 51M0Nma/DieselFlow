package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.remote.GoogleSheetsService
import com.example.ui.theme.DieselOnPrimary
import com.example.ui.theme.DieselOnSurface
import com.example.ui.theme.DieselOnSurfaceVariant
import com.example.ui.theme.DieselPrimary
import com.example.ui.theme.DieselSecondary
import com.example.ui.theme.DieselSurfaceContainer
import com.example.ui.theme.DieselSurfaceContainerHigh
import com.example.ui.theme.DieselSurfaceContainerLow
import com.example.ui.theme.DieselSurfaceContainerLowest
import com.example.ui.theme.DieselTertiary

@Composable
fun LinkGoogleSheetDialog(
    currentUrl: String,
    onSaveUrl: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var urlInput by remember { mutableStateOf(currentUrl) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    fun extractSheetIdAndSave() {
        val input = urlInput.trim()
        if (input.isBlank()) {
            errorMessage = "Please enter a Google Sheet URL or ID"
            return
        }

        // Extract ID from full URL or use raw ID
        val sheetId = when {
            input.contains("/spreadsheets/d/") -> {
                input.substringAfter("/spreadsheets/d/").substringBefore("/")
            }
            input.contains("docs.google.com") -> {
                input.substringAfter("id=").substringBefore("&")
            }
            else -> input
        }

        if (sheetId.length < 15) {
            errorMessage = "Invalid Google Sheet link or ID. Please check the URL."
            return
        }

        val fullUrl = "https://docs.google.com/spreadsheets/d/$sheetId/edit"
        GoogleSheetsService.saveSpreadsheetInfo(context, sheetId, fullUrl)
        onSaveUrl(fullUrl)
        Toast.makeText(context, "Google Sheet linked successfully!", Toast.LENGTH_SHORT).show()
        onDismiss()
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = DieselSurfaceContainerLowest,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Dialog Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(DieselSurfaceContainerLow),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.TableChart,
                                contentDescription = null,
                                tint = DieselPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Link Your Google Sheet",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = DieselOnSurface
                            )
                            Text(
                                text = "Account: simon.mahajan08@gmail.com",
                                style = MaterialTheme.typography.bodySmall,
                                color = DieselSecondary
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = DieselSecondary
                        )
                    }
                }

                // Quick Step 1: Create Blank Google Sheet
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DieselSurfaceContainerLow),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "STEP 1: CREATE A SHEET IN YOUR GOOGLE DRIVE",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp),
                            color = DieselPrimary
                        )
                        Text(
                            text = "Click below to create a new spreadsheet in your Google account, then copy its link from the browser.",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = DieselOnSurfaceVariant
                        )

                        OutlinedButton(
                            onClick = {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://sheets.new"))
                                context.startActivity(intent)
                            },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Create New Sheet in Drive (sheets.new)", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Step 2: Paste Google Sheet Link
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "STEP 2: PASTE YOUR SPREADSHEET LINK / ID",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp),
                        color = DieselSecondary
                    )

                    OutlinedTextField(
                        value = urlInput,
                        onValueChange = {
                            urlInput = it
                            errorMessage = null
                        },
                        label = { Text("Google Sheet URL or ID") },
                        placeholder = { Text("https://docs.google.com/spreadsheets/d/...") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Link,
                                contentDescription = null,
                                tint = DieselSecondary
                            )
                        },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("sheet_url_input")
                    )

                    if (errorMessage != null) {
                        Text(
                            text = errorMessage!!,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }

                // Copy Header Structure shortcut
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(DieselSurfaceContainerHigh)
                        .clickable {
                            val headersCsv = "Date,Unit 1,Unit 2,Unit 3,Unit 4,Unit 5,Unit 6,Unit 7,Facility & Plant,Issued To,Shift Remarks,Rate (INR),Total Cost"
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("Headers", headersCsv))
                            Toast.makeText(context, "Header columns copied to clipboard", Toast.LENGTH_SHORT).show()
                        }
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = null,
                            tint = DieselPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Copy 13 Column Headers to Clipboard",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = DieselOnSurface
                        )
                    }
                    Text(
                        text = "Cols A–M",
                        style = MaterialTheme.typography.labelSmall,
                        color = DieselSecondary
                    )
                }

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = { extractSheetIdAndSave() },
                        colors = ButtonDefaults.buttonColors(containerColor = DieselPrimary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = DieselOnPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Link & Save", fontWeight = FontWeight.Bold, color = DieselOnPrimary)
                    }
                }
            }
        }
    }
}
