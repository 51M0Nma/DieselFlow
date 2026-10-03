package com.example.ui.components

import android.widget.Toast
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FleetAssetConfig
import com.example.data.repository.DieselFlowRepository
import com.example.ui.theme.DieselError
import com.example.ui.theme.DieselOnPrimary
import com.example.ui.theme.DieselOnSecondaryContainer
import com.example.ui.theme.DieselOnSurface
import com.example.ui.theme.DieselOnSurfaceVariant
import com.example.ui.theme.DieselPrimary
import com.example.ui.theme.DieselSecondary
import com.example.ui.theme.DieselSecondaryContainer
import com.example.ui.theme.DieselSurfaceContainer
import com.example.ui.theme.DieselSurfaceContainerHighest
import com.example.ui.theme.DieselSurfaceContainerLow
import com.example.ui.theme.DieselSurfaceContainerLowest
import com.example.ui.theme.DieselTertiary
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnitManagementBottomSheet(
    assets: List<FleetAssetConfig>,
    repository: DieselFlowRepository,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var editingAsset by remember { mutableStateOf<FleetAssetConfig?>(null) }
    var isAddingNew by remember { mutableStateOf(false) }

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
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(DieselPrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalShipping,
                            contentDescription = null,
                            tint = DieselOnPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "Fleet Unit Management",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = DieselOnSurface
                        )
                        Text(
                            text = "Configure, Add, Edit, or Delete Fleet Units",
                            style = MaterialTheme.typography.bodySmall,
                            color = DieselSecondary
                        )
                    }
                }

                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = DieselOnSurfaceVariant
                    )
                }
            }

            if (editingAsset == null && !isAddingNew) {
                // Unit List View
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "FLEET VEHICLES & ASSETS (${assets.size})",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = DieselSecondary
                    )

                    Button(
                        onClick = {
                            coroutineScope.launch {
                                val nextNum = repository.getNextUnitNumber()
                                val nextCol = "U$nextNum"
                                isAddingNew = true
                                editingAsset = FleetAssetConfig(
                                    colKey = nextCol,
                                    unitNumber = nextNum,
                                    unitName = "Unit $nextNum",
                                    licensePlate = "GA ${1000 + nextNum}",
                                    vehicleType = "Fleet Truck",
                                    meterInfo = "Main Dispenser",
                                    isIdle = false
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = DieselPrimary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .height(34.dp)
                            .testTag("add_new_unit_button")
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Unit", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    assets.forEach { unit ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = DieselSurfaceContainerLowest),
                            shape = RoundedCornerShape(10.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(DieselSurfaceContainer),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(
                                                text = "Col ${unit.colKey}",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold
                                                ),
                                                color = DieselSecondary
                                            )
                                            Text(
                                                text = "#${unit.unitNumber}",
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                color = DieselPrimary
                                            )
                                        }
                                    }

                                    Column {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text(
                                                text = unit.licensePlate,
                                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                                color = DieselOnSurface
                                            )
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(DieselSurfaceContainer)
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = unit.unitName,
                                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                                    color = DieselSecondary
                                                )
                                            }

                                            if (unit.isIdle) {
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(4.dp))
                                                        .background(DieselSecondaryContainer)
                                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                                ) {
                                                    Text(
                                                        text = "IDLE",
                                                        style = MaterialTheme.typography.labelSmall.copy(
                                                            fontSize = 9.sp,
                                                            fontWeight = FontWeight.Bold
                                                        ),
                                                        color = DieselOnSecondaryContainer
                                                    )
                                                }
                                            }
                                        }

                                        Text(
                                            text = "${unit.vehicleType} • ${unit.meterInfo}",
                                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                            color = DieselSecondary
                                        )
                                    }
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    IconButton(
                                        onClick = {
                                            isAddingNew = false
                                            editingAsset = unit
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "Edit Unit",
                                            tint = DieselPrimary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }

                                    IconButton(
                                        onClick = {
                                            coroutineScope.launch {
                                                repository.deleteFleetUnit(unit.colKey)
                                                Toast.makeText(context, "Unit ${unit.unitName} (${unit.licensePlate}) removed", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete Unit",
                                            tint = DieselError,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // Unit Edit / Create Form
                val assetToEdit = editingAsset!!
                var colKey by remember { mutableStateOf(assetToEdit.colKey) }
                var unitNumber by remember { mutableStateOf(assetToEdit.unitNumber.toString()) }
                var unitName by remember { mutableStateOf(assetToEdit.unitName) }
                var licensePlate by remember { mutableStateOf(assetToEdit.licensePlate) }
                var vehicleType by remember { mutableStateOf(assetToEdit.vehicleType) }
                var meterInfo by remember { mutableStateOf(assetToEdit.meterInfo) }
                var isIdle by remember { mutableStateOf(assetToEdit.isIdle) }
                var idleReason by remember { mutableStateOf(assetToEdit.idleReason) }
                var capacityLiters by remember { mutableStateOf(assetToEdit.capacityLiters.toString()) }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DieselSurfaceContainerLowest),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = if (isAddingNew) "Add New Fleet Unit" else "Edit Unit Details: ${assetToEdit.unitName}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = DieselOnSurface
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = colKey,
                                onValueChange = { colKey = it.uppercase() },
                                label = { Text("Col Key / ID") },
                                singleLine = true,
                                enabled = isAddingNew,
                                modifier = Modifier.weight(1f)
                            )

                            OutlinedTextField(
                                value = unitNumber,
                                onValueChange = { unitNumber = it },
                                label = { Text("Unit #") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        OutlinedTextField(
                            value = unitName,
                            onValueChange = { unitName = it },
                            label = { Text("Unit Display Name (e.g. Unit 8)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = licensePlate,
                            onValueChange = { licensePlate = it },
                            label = { Text("Physical License Plate (e.g. GA 7890)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = vehicleType,
                            onValueChange = { vehicleType = it },
                            label = { Text("Vehicle Type / Model (e.g. Heavy Hauler 50T)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = meterInfo,
                            onValueChange = { meterInfo = it },
                            label = { Text("Dispenser / Meter Location") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = capacityLiters,
                            onValueChange = { capacityLiters = it },
                            label = { Text("Tank Capacity (L)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Idle Status Switch
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Idle in Yard / Maintenance",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = DieselOnSurface
                                )
                                Text(
                                    text = "Marks unit inactive for routine daily fuel logs",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = DieselSecondary
                                )
                            }

                            Switch(
                                checked = isIdle,
                                onCheckedChange = { isIdle = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = DieselOnPrimary,
                                    checkedTrackColor = DieselTertiary
                                )
                            )
                        }

                        if (isIdle) {
                            OutlinedTextField(
                                value = idleReason,
                                onValueChange = { idleReason = it },
                                label = { Text("Idle Reason (e.g. Scheduled Overhaul)") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        // Form Actions
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            TextButton(
                                onClick = {
                                    editingAsset = null
                                    isAddingNew = false
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Cancel", color = DieselSecondary)
                            }

                            Button(
                                onClick = {
                                    if (licensePlate.isBlank() || colKey.isBlank()) {
                                        Toast.makeText(context, "Col Key and License Plate required", Toast.LENGTH_SHORT).show()
                                        return@Button
                                    }

                                    val updated = assetToEdit.copy(
                                        colKey = colKey.trim(),
                                        unitNumber = unitNumber.toIntOrNull() ?: 1,
                                        unitName = unitName.trim().ifEmpty { "Unit $unitNumber" },
                                        licensePlate = licensePlate.trim(),
                                        vehicleType = vehicleType.trim().ifEmpty { "Fleet Vehicle" },
                                        meterInfo = meterInfo.trim(),
                                        isIdle = isIdle,
                                        idleReason = idleReason.trim(),
                                        capacityLiters = capacityLiters.toDoubleOrNull() ?: 500.0
                                    )

                                    coroutineScope.launch {
                                        repository.saveFleetUnit(updated)
                                        editingAsset = null
                                        isAddingNew = false
                                        Toast.makeText(context, "Unit ${updated.unitName} (${updated.licensePlate}) saved!", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = DieselPrimary),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("save_unit_button")
                            ) {
                                Text("Save Unit", color = DieselOnPrimary, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
