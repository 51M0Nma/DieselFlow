package com.example.ui.components

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AssignmentInd
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Factory
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
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
import com.example.data.model.DailyLogEntry
import com.example.data.model.FleetAssetConfig
import com.example.ui.theme.DieselOnPrimary
import com.example.ui.theme.DieselOnSurface
import com.example.ui.theme.DieselOnSurfaceVariant
import com.example.ui.theme.DieselPrimary
import com.example.ui.theme.DieselSecondary
import com.example.ui.theme.DieselSurfaceContainer
import com.example.ui.theme.DieselSurfaceContainerLow
import com.example.ui.theme.DieselSurfaceContainerLowest
import com.example.ui.theme.DieselTertiary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditSlipBottomSheet(
    entry: DailyLogEntry,
    assets: List<FleetAssetConfig>,
    onDismiss: () -> Unit,
    onSave: (DailyLogEntry) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Dynamic map of unit liters
    val unitInputs = remember(entry, assets) {
        mutableStateMapOf<String, String>().apply {
            assets.forEach { asset ->
                val reading = entry.getUnitReading(asset.colKey)
                put(asset.colKey, if (reading > 0) reading.toString() else "")
            }
        }
    }

    var plant by remember { mutableStateOf(if (entry.colI_plant > 0) entry.colI_plant.toString() else "") }
    var plantNotes by remember { mutableStateOf(entry.plantRemarks) }

    var issuedToName by remember { mutableStateOf(entry.issuedToName.ifEmpty { entry.colJ_issueTo.ifEmpty { "Apex Earthworks Ltd - Site B" } }) }
    var issuedToLtr by remember { mutableStateOf(if (entry.issuedToLiters > 0) entry.issuedToLiters.toString() else "") }
    var issuedToRemarks by remember { mutableStateOf(entry.issuedToRemarks) }

    var generalRemarks by remember { mutableStateOf(entry.colK_remarks) }
    var rate by remember { mutableStateOf(entry.colL_rate.toString()) }
    var slipAttached by remember { mutableStateOf(entry.slipAttached) }
    var isExpandedIssueTo by remember { mutableStateOf(false) }

    val recipients = listOf(
        "Apex Earthworks Ltd - Site B",
        "Logistics Dept",
        "Plant Backup",
        "Earthworks Contractor",
        "Terminal Shunter Crew",
        "South Haulage Division"
    )

    val fleetSum = assets.sumOf { asset ->
        unitInputs[asset.colKey]?.toDoubleOrNull() ?: 0.0
    }
    val plantVal = plant.toDoubleOrNull() ?: 0.0
    val issuedVal = issuedToLtr.toDoubleOrNull() ?: 0.0

    val currentTotalLiters = fleetSum + plantVal + issuedVal
    val currentRateVal = rate.toDoubleOrNull() ?: 92.50
    val currentCost = currentTotalLiters * currentRateVal

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Title Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Edit Day Slip (Row ${entry.targetRow})",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = DieselOnSurface
                    )
                    Text(
                        text = "Date: Day ${entry.dayNumber} Oct, 2026 • Live Reconciliation",
                        style = MaterialTheme.typography.bodySmall,
                        color = DieselOnSurfaceVariant
                    )
                }

                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = DieselOnSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Realtime Summary Bento
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DieselSurfaceContainerLow),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "TOTAL DISPENSED",
                            style = MaterialTheme.typography.labelSmall,
                            color = DieselSecondary
                        )
                        Text(
                            text = "${FormatUtils.formatNumber(currentTotalLiters)} L",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = DieselOnSurface
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "EST. COST (COL M)",
                            style = MaterialTheme.typography.labelSmall,
                            color = DieselSecondary
                        )
                        Text(
                            text = FormatUtils.formatINR(currentCost),
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = DieselPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Fleet Units Input Grid
            Text(
                text = "FLEET VEHICLE ENTRIES (${assets.size} UNITS)",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = DieselSecondary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                assets.forEach { unit ->
                    VehicleInputRow(
                        colLabel = "Col ${unit.colKey}",
                        unitName = unit.unitName,
                        plate = unit.licensePlate,
                        type = unit.vehicleType,
                        value = unitInputs[unit.colKey] ?: "",
                        onValueChange = { unitInputs[unit.colKey] = it }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 1. Facility & Plant Allocation (Col I) - NO dropdown
            Text(
                text = "FACILITY & PLANT GENERATOR (COL I)",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = DieselSecondary
            )

            Spacer(modifier = Modifier.height(6.dp))

            OutlinedTextField(
                value = plant,
                onValueChange = { plant = it },
                label = { Text("Plant Genset #2 Liters (Col I)") },
                suffix = { Text("L", fontWeight = FontWeight.Bold) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = plantNotes,
                onValueChange = { plantNotes = it },
                label = { Text("Plant / Generator Notes") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 2. Dedicated "Issued To" Section (Col J) - Recipient, Liters, Remarks
            Text(
                text = "ISSUED TO (DIRECT DISPATCH COL J)",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = DieselSecondary
            )

            Spacer(modifier = Modifier.height(6.dp))

            ExposedDropdownMenuBox(
                expanded = isExpandedIssueTo,
                onExpandedChange = { isExpandedIssueTo = !isExpandedIssueTo },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = issuedToName,
                    onValueChange = { issuedToName = it },
                    label = { Text("Recipient / Destination Name") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isExpandedIssueTo) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )

                ExposedDropdownMenu(
                    expanded = isExpandedIssueTo,
                    onDismissRequest = { isExpandedIssueTo = false }
                ) {
                    recipients.forEach { rec ->
                        DropdownMenuItem(
                            text = { Text(rec) },
                            onClick = {
                                issuedToName = rec
                                isExpandedIssueTo = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = issuedToLtr,
                    onValueChange = { issuedToLtr = it },
                    label = { Text("Issued To Quantity (L)") },
                    suffix = { Text("L", fontWeight = FontWeight.Bold) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )

                OutlinedTextField(
                    value = rate,
                    onValueChange = { rate = it },
                    label = { Text("Rate (₹/L)") },
                    prefix = { Text("₹ ", fontWeight = FontWeight.Bold, color = DieselPrimary) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            OutlinedTextField(
                value = issuedToRemarks,
                onValueChange = { issuedToRemarks = it },
                label = { Text("Issued To Specific Notes / Slip #") },
                maxLines = 2,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Shift General Remarks (Col K)
            Text(
                text = "SHIFT GENERAL REMARKS (COL K)",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = DieselSecondary
            )

            Spacer(modifier = Modifier.height(6.dp))

            OutlinedTextField(
                value = generalRemarks,
                onValueChange = { generalRemarks = it },
                label = { Text("Shift overall notes, dispenser health...") },
                maxLines = 3,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Physical Slip Attached Toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(DieselSurfaceContainerLow)
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AttachFile,
                        contentDescription = null,
                        tint = DieselTertiary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Signed Slip Attached",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = DieselOnSurface
                        )
                        Text(
                            text = "Physical metered receipt verified in yard log",
                            style = MaterialTheme.typography.bodySmall,
                            color = DieselOnSurfaceVariant
                        )
                    }
                }

                Switch(
                    checked = slipAttached,
                    onCheckedChange = { slipAttached = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = DieselOnPrimary,
                        checkedTrackColor = DieselTertiary
                    )
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Save and Push Button
            Button(
                onClick = {
                    var updated = entry.copy(
                        colI_plant = plantVal,
                        plantRemarks = plantNotes,
                        issuedToName = issuedToName,
                        issuedToLiters = issuedVal,
                        issuedToRemarks = issuedToRemarks,
                        colJ_issueTo = issuedToName,
                        colK_remarks = generalRemarks,
                        colL_rate = currentRateVal,
                        isLogged = true,
                        isSynced = true,
                        slipAttached = slipAttached
                    )

                    assets.forEach { asset ->
                        val uVal = unitInputs[asset.colKey]?.toDoubleOrNull() ?: 0.0
                        updated = updated.setUnitReading(asset.colKey, uVal)
                    }

                    onSave(updated)
                    onDismiss()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("save_and_push_slip_button"),
                colors = ButtonDefaults.buttonColors(containerColor = DieselPrimary),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Done,
                    contentDescription = null,
                    tint = DieselOnPrimary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Save & Push to Sheet1 (Row ${entry.targetRow})",
                    style = MaterialTheme.typography.headlineSmall.copy(fontSize = 15.sp),
                    fontWeight = FontWeight.Bold,
                    color = DieselOnPrimary
                )
            }
        }
    }
}

@Composable
fun VehicleInputRow(
    colLabel: String,
    unitName: String,
    plate: String,
    type: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(DieselSurfaceContainerLowest)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(DieselSurfaceContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = colLabel.replace("Col ", ""),
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = DieselSecondary
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column {
                Text(
                    text = "$plate ($unitName)",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = DieselOnSurface
                )
                Text(
                    text = type,
                    style = MaterialTheme.typography.bodySmall,
                    color = DieselOnSurfaceVariant
                )
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .width(100.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(DieselSurfaceContainerLow)
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                textStyle = MaterialTheme.typography.labelLarge.copy(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                ),
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "L",
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = DieselSecondary,
                modifier = Modifier.padding(start = 2.dp)
            )
        }
    }
}
