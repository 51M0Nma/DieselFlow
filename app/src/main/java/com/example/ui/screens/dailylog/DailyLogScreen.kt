package com.example.ui.screens.dailylog

import android.widget.Toast
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AssignmentInd
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Factory
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.TableRows
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DailyLogEntry
import com.example.data.model.FleetAssetConfig
import com.example.data.model.UserEntity
import com.example.data.repository.DieselFlowRepository
import com.example.ui.components.FormatUtils
import com.example.ui.components.QuickRateEditDialog
import com.example.ui.components.UnitManagementBottomSheet
import com.example.ui.theme.DieselError
import com.example.ui.theme.DieselOnPrimary
import com.example.ui.theme.DieselOnPrimaryFixed
import com.example.ui.theme.DieselOnSecondaryContainer
import com.example.ui.theme.DieselOnSurface
import com.example.ui.theme.DieselOnSurfaceVariant
import com.example.ui.theme.DieselOutline
import com.example.ui.theme.DieselPrimary
import com.example.ui.theme.DieselPrimaryFixed
import com.example.ui.theme.DieselSecondary
import com.example.ui.theme.DieselSecondaryContainer
import com.example.ui.theme.DieselSurfaceContainer
import com.example.ui.theme.DieselSurfaceContainerHigh
import com.example.ui.theme.DieselSurfaceContainerHighest
import com.example.ui.theme.DieselSurfaceContainerLow
import com.example.ui.theme.DieselSurfaceContainerLowest
import com.example.ui.theme.DieselTertiary
import com.example.ui.theme.DieselTertiaryFixedDim
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun DailyLogScreen(
    currentDay: Int,
    onSelectDay: (Int) -> Unit,
    repository: DieselFlowRepository,
    allLogs: List<DailyLogEntry>,
    assets: List<FleetAssetConfig>,
    currentUser: UserEntity?,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val canEdit = currentUser?.canEditDailyLog == true || currentUser?.isAdmin == true

    val logEntry = allLogs.find { it.dayNumber == currentDay } ?: DailyLogEntry(
        dateKey = "2026-10-${currentDay.toString().padStart(2, '0')}",
        dayNumber = currentDay,
        isLogged = false
    )

    // Dynamic map of unit liters for all configured fleet assets
    val unitLitersMap = remember(logEntry, assets) {
        mutableStateMapOf<String, String>().apply {
            assets.forEach { asset ->
                val reading = logEntry.getUnitReading(asset.colKey)
                put(asset.colKey, if (reading > 0) reading.toString() else "")
            }
        }
    }

    // Facility & Plant state (Stationary generator)
    var plantLiters by remember(logEntry) {
        mutableStateOf(if (logEntry.colI_plant > 0) logEntry.colI_plant.toString() else "")
    }
    var plantNotes by remember(logEntry) {
        mutableStateOf(logEntry.plantRemarks.ifEmpty { "Main Yard Backup Power • Runtime: 14 hrs" })
    }

    // Dedicated "Issued To" section state
    var issuedToRecipient by remember(logEntry) {
        mutableStateOf(logEntry.issuedToName.ifEmpty { logEntry.colJ_issueTo.ifEmpty { "Apex Earthworks Ltd - Site B" } })
    }
    var issuedToLtr by remember(logEntry) {
        mutableStateOf(if (logEntry.issuedToLiters > 0) logEntry.issuedToLiters.toString() else "")
    }
    var issuedToRemarks by remember(logEntry) {
        mutableStateOf(logEntry.issuedToRemarks)
    }

    // General shift remarks
    var generalRemarks by remember(logEntry) { mutableStateOf(logEntry.colK_remarks) }
    var rate by remember(logEntry) { mutableDoubleStateOf(logEntry.colL_rate) }
    var isSyncedState by remember(logEntry) { mutableStateOf(logEntry.isSynced) }
    var isSyncing by remember { mutableStateOf(false) }

    var showQuickRateDialog by remember { mutableStateOf(false) }
    var showRecipientDropdown by remember { mutableStateOf(false) }
    var showUnitManagementSheet by remember { mutableStateOf(false) }

    val presetRecipients = listOf(
        "Apex Earthworks Ltd - Site B",
        "Logistics Dept - Highway Run",
        "Plant Backup Genset",
        "Earthworks Contractor - Zone 4",
        "Terminal Shunter Maintenance Crew",
        "South Haulage Division",
        "External Vendor Tanker Fill"
    )

    // Dynamic Calculations
    val fleetTotalSum = assets.sumOf { asset ->
        unitLitersMap[asset.colKey]?.toDoubleOrNull() ?: 0.0
    }
    val plantVal = plantLiters.toDoubleOrNull() ?: 0.0
    val issuedToVal = issuedToLtr.toDoubleOrNull() ?: 0.0

    val totalDispensed = fleetTotalSum + plantVal + issuedToVal
    val totalCost = totalDispensed * rate

    val activePointsCount = assets.count { (unitLitersMap[it.colKey]?.toDoubleOrNull() ?: 0.0) > 0 } +
            (if (plantVal > 0) 1 else 0) +
            (if (issuedToVal > 0) 1 else 0)

    // Pulse animation
    val infiniteTransition = rememberInfiniteTransition(label = "syncPulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        if (!canEdit) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(DieselSecondaryContainer)
                    .padding(10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = DieselOnSecondaryContainer,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "View-Only Mode: You have read permissions for Daily Logs. Contact administrator for edit rights.",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                        color = DieselOnSecondaryContainer
                    )
                }
            }
        }

        // Target Row Anchor & Day Selector Ribbon
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DieselSurfaceContainerLow),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.TableRows,
                            contentDescription = null,
                            tint = DieselPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "TARGET ROW:",
                            style = MaterialTheme.typography.labelSmall,
                            color = DieselSecondary
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(DieselSurfaceContainerHighest)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "Row #${currentDay + 2} (Oct $currentDay)",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = DieselOnSurface
                            )
                        }
                    }

                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(DieselTertiary.copy(alpha = 0.12f))
                            .padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .scale(pulseScale)
                                .clip(CircleShape)
                                .background(DieselTertiary)
                        )
                        Text(
                            text = "Active Sheet",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = DieselTertiary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Date Selector Ribbon
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val daysList = (1..31).toList()
                    val daysOfWeek = listOf("THU", "FRI", "SAT", "SUN", "MON", "TUE", "WED")

                    items(daysList) { day ->
                        val isSelected = day == currentDay
                        val dow = daysOfWeek[(day - 1) % 7]

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) DieselPrimary else DieselSurfaceContainer)
                                .clickable { onSelectDay(day) }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                .testTag("day_chip_$day"),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = if (day == 24) "TODAY" else dow,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = if (isSelected) DieselPrimaryFixed else DieselSecondary
                                )
                                Text(
                                    text = day.toString(),
                                    style = MaterialTheme.typography.headlineSmall.copy(
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = if (isSelected) DieselOnPrimary else DieselOnSurface
                                )
                            }
                        }
                    }
                }
            }
        }

        // Daily Computation Bento Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DieselSurfaceContainerLowest),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header
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
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(DieselPrimary.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocalGasStation,
                                contentDescription = null,
                                tint = DieselPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Column {
                            Text(
                                text = "Daily Computation",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = DieselOnSurface
                            )
                            Text(
                                text = "Auto-calculating formula (Col B:I * Col L)",
                                style = MaterialTheme.typography.labelSmall,
                                color = DieselSecondary
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(DieselTertiary.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .scale(pulseScale)
                                    .clip(CircleShape)
                                    .background(DieselTertiary)
                            )
                            Text(
                                text = "SYNC LIVE ROW ${currentDay + 2}",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = DieselTertiary
                            )
                        }
                    }
                }

                // Volume + Day Rate Bento Tiles
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Total Volume Box
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(DieselSurfaceContainerLow)
                            .padding(12.dp)
                    ) {
                        Column {
                            Text(
                                text = "TOTAL DISPENSED",
                                style = MaterialTheme.typography.labelSmall,
                                color = DieselSecondary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = FormatUtils.formatNumber(totalDispensed),
                                    style = MaterialTheme.typography.displayLarge.copy(fontSize = 24.sp),
                                    fontWeight = FontWeight.Bold,
                                    color = DieselOnSurface
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "L",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = DieselSecondary,
                                    modifier = Modifier.padding(bottom = 2.dp)
                                )
                            }
                            Text(
                                text = "$activePointsCount Dispense points logged",
                                style = MaterialTheme.typography.labelSmall,
                                color = DieselTertiary
                            )
                        }
                    }

                    // Day Rate Box
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(DieselSurfaceContainerLow)
                            .padding(12.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "DAY RATE (COL L)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = DieselSecondary
                                )

                                if (canEdit) {
                                    Box(
                                        modifier = Modifier
                                            .size(22.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(DieselSurfaceContainerHighest)
                                            .clickable { showQuickRateDialog = true }
                                            .testTag("quick_edit_rate_button"),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "Edit Rate",
                                            tint = DieselPrimary,
                                            modifier = Modifier.size(12.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(2.dp))

                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = "₹",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = DieselOnSurfaceVariant,
                                    modifier = Modifier.padding(bottom = 2.dp)
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    text = FormatUtils.formatNumber(rate),
                                    style = MaterialTheme.typography.displayLarge.copy(fontSize = 24.sp),
                                    fontWeight = FontWeight.Bold,
                                    color = DieselOnSurface
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    text = "/ L",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = DieselSecondary,
                                    modifier = Modifier.padding(bottom = 2.dp)
                                )
                            }
                            Text(
                                text = "Depot bulk price (INR)",
                                style = MaterialTheme.typography.labelSmall,
                                color = DieselSecondary
                            )
                        }
                    }
                }

                // Highlight Calculated Cost Tile (Col M)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DieselPrimary),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "CALCULATED COST (COL M)",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                ),
                                color = DieselPrimaryFixed
                            )
                            Text(
                                text = "${FormatUtils.formatNumber(totalDispensed)} L × ₹${FormatUtils.formatNumber(rate)}/L",
                                style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                                color = DieselOnPrimary.copy(alpha = 0.8f)
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "₹",
                                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                                color = DieselPrimaryFixed
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = FormatUtils.formatNumber(totalCost),
                                style = MaterialTheme.typography.displayLarge.copy(fontSize = 24.sp),
                                fontWeight = FontWeight.Bold,
                                color = DieselOnPrimary
                            )
                        }
                    }
                }
            }
        }

        // Fleet Fuel Entries Header with Unit Management Shortcut
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.LocalShipping,
                    contentDescription = null,
                    tint = DieselPrimary,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Fleet Fuel Entries",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = DieselOnSurface
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(DieselSecondaryContainer)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "${assets.size} Units",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = DieselOnSecondaryContainer
                    )
                }
            }

            if (currentUser?.canManageUnits == true || currentUser?.isAdmin == true) {
                Button(
                    onClick = { showUnitManagementSheet = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DieselSurfaceContainerLow,
                        contentColor = DieselPrimary
                    ),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.height(28.dp)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(2.dp))
                    Text("Manage Units", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold))
                }
            }
        }

        // Dynamic Fleet Unit Cards
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            assets.forEach { asset ->
                val currentVal = unitLitersMap[asset.colKey] ?: ""

                DynamicFleetUnitCard(
                    col = "Col ${asset.colKey}",
                    unitNumber = "#${asset.unitNumber}",
                    plate = asset.licensePlate,
                    name = asset.unitName,
                    vehicleType = asset.vehicleType,
                    meter = asset.meterInfo,
                    value = currentVal,
                    onValueChange = { newVal ->
                        if (canEdit) {
                            unitLitersMap[asset.colKey] = newVal
                        }
                    },
                    isIdle = asset.isIdle,
                    readOnly = !canEdit
                )
            }
        }

        // 1. Facility & Plant Allocation (Stationary generator ONLY - NO destination dropdown)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DieselSurfaceContainerLowest),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Factory,
                            contentDescription = null,
                            tint = DieselSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                        Column {
                            Text(
                                text = "Facility & Plant Allocation",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = DieselOnSurface
                            )
                            Text(
                                text = "Stationary generator & plant diesel metering",
                                style = MaterialTheme.typography.bodySmall,
                                color = DieselSecondary
                            )
                        }
                    }
                    Text(
                        text = "Col I",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = DieselSecondary
                    )
                }

                // Plant Genset (Col I)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(DieselSurfaceContainerLow)
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "PLANT / GENERATOR (COL I)",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = DieselSecondary
                        )
                        Text(
                            text = "Stationary Plant Genset #2",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = DieselOnSurface
                        )
                        Text(
                            text = plantNotes,
                            style = MaterialTheme.typography.bodySmall,
                            color = DieselSecondary
                        )
                    }

                    Box(
                        modifier = Modifier
                            .width(88.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(DieselSurfaceContainerLowest)
                            .border(1.dp, DieselOutline.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.End,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            BasicTextField(
                                value = plantLiters,
                                onValueChange = { if (canEdit) plantLiters = it },
                                readOnly = !canEdit,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                textStyle = TextStyle(
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    textAlign = TextAlign.End,
                                    color = DieselOnSurface
                                ),
                                cursorBrush = SolidColor(DieselPrimary),
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "L",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = DieselSecondary
                            )
                        }
                    }
                }
            }
        }

        // 2. Dedicated "Issued To" Section (NEW: Liter filling + recipient destination + remarks)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DieselSurfaceContainerLowest),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AssignmentInd,
                            contentDescription = null,
                            tint = DieselPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Column {
                            Text(
                                text = "Issued To (Direct Fuel Dispatch)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = DieselOnSurface
                            )
                            Text(
                                text = "Specific recipient fueling with liters & remarks",
                                style = MaterialTheme.typography.bodySmall,
                                color = DieselSecondary
                            )
                        }
                    }
                    Text(
                        text = "Col J",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = DieselSecondary
                    )
                }

                // Recipient Picker / Input
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(DieselSurfaceContainerLow)
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "RECIPIENT / DESTINATION (COL J)",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = DieselSecondary
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(DieselSurfaceContainerLowest)
                            .clickable(enabled = canEdit) { showRecipientDropdown = true }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = DieselPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = issuedToRecipient.ifEmpty { "Select or type recipient" },
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = if (issuedToRecipient.isEmpty()) DieselSecondary else DieselOnSurface
                                )
                            }
                            if (canEdit) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = null,
                                    tint = DieselSecondary,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = showRecipientDropdown,
                            onDismissRequest = { showRecipientDropdown = false }
                        ) {
                            presetRecipients.forEach { item ->
                                DropdownMenuItem(
                                    text = { Text(item) },
                                    onClick = {
                                        issuedToRecipient = item
                                        showRecipientDropdown = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Liter Filling for Issued To
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(DieselSurfaceContainerLow)
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "ISSUED TO FUEL QUANTITY",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = DieselSecondary
                        )
                        Text(
                            text = "Direct Fuel Dispensed (L)",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = DieselOnSurface
                        )
                    }

                    Box(
                        modifier = Modifier
                            .width(96.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(DieselSurfaceContainerLowest)
                            .border(1.dp, DieselOutline.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.End,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            BasicTextField(
                                value = issuedToLtr,
                                onValueChange = { if (canEdit) issuedToLtr = it },
                                readOnly = !canEdit,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                textStyle = TextStyle(
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    textAlign = TextAlign.End,
                                    color = DieselOnSurface
                                ),
                                cursorBrush = SolidColor(DieselPrimary),
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "L",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = DieselSecondary
                            )
                        }
                    }
                }

                // Specific Remarks for Issued To
                OutlinedTextField(
                    value = issuedToRemarks,
                    onValueChange = { if (canEdit) issuedToRemarks = it },
                    readOnly = !canEdit,
                    placeholder = { Text("Enter specific notes for Issued To (e.g. Authorizer slip, trip code)...") },
                    label = { Text("Issued To Remarks / Voucher Note") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2,
                    shape = RoundedCornerShape(8.dp)
                )
            }
        }

        // Shift General Remarks (Col K)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DieselSurfaceContainerLowest),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notes,
                            contentDescription = null,
                            tint = DieselSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Shift General Remarks (Col K)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = DieselOnSurface
                        )
                    }
                    Text(
                        text = "Col K",
                        style = MaterialTheme.typography.labelSmall,
                        color = DieselSecondary
                    )
                }

                OutlinedTextField(
                    value = generalRemarks,
                    onValueChange = { if (canEdit) generalRemarks = it },
                    readOnly = !canEdit,
                    placeholder = { Text("Enter terminal shift remarks, dispenser maintenance, weather...") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3,
                    shape = RoundedCornerShape(8.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Logged by ${currentUser?.fullName ?: "Depot Foreman"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = DieselSecondary
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AttachFile,
                            contentDescription = null,
                            tint = DieselTertiary,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "Slip attached",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                            color = DieselTertiary
                        )
                    }
                }
            }
        }

        // Live Row Mapping Snapshot Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(DieselSurfaceContainerHigh)
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.TableRows,
                        contentDescription = null,
                        tint = DieselOnSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                    Column {
                        Text(
                            text = "Live Row ${currentDay + 2} Mapping",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = DieselOnSurface
                        )
                        Text(
                            text = "A: Oct $currentDay | Fleet: ${FormatUtils.formatNumber(fleetTotalSum, 1)}L | Plant: ${FormatUtils.formatNumber(plantVal, 1)}L | Issued: ${FormatUtils.formatNumber(issuedToVal, 1)}L",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                            color = DieselSecondary
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(DieselSurfaceContainerLowest)
                        .padding(horizontal = 6.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "READY",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp
                        ),
                        color = DieselTertiary
                    )
                }
            }
        }

        // Primary Save & Push Button
        if (canEdit) {
            Button(
                onClick = {
                    coroutineScope.launch {
                        isSyncing = true
                        var updated = logEntry.copy(
                            dayNumber = currentDay,
                            dateKey = "2026-10-${currentDay.toString().padStart(2, '0')}",
                            colI_plant = plantVal,
                            plantRemarks = plantNotes,
                            issuedToName = issuedToRecipient,
                            issuedToLiters = issuedToVal,
                            issuedToRemarks = issuedToRemarks,
                            colJ_issueTo = issuedToRecipient,
                            colK_remarks = generalRemarks,
                            colL_rate = rate,
                            isLogged = true,
                            isSynced = true,
                            loggedBy = currentUser?.fullName ?: "Operator"
                        )

                        // Save all unit readings
                        assets.forEach { asset ->
                            val unitVal = unitLitersMap[asset.colKey]?.toDoubleOrNull() ?: 0.0
                            updated = updated.setUnitReading(asset.colKey, unitVal)
                        }

                        repository.saveOrUpdateLog(updated)
                        delay(700)
                        isSyncing = false
                        isSyncedState = true
                        Toast.makeText(
                            context,
                            "Successfully Synced Row #${currentDay + 2} to Sheet1!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("save_and_push_to_sheet_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isSyncedState && !isSyncing) DieselPrimary else DieselTertiary
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = if (isSyncing) Icons.Default.CloudUpload else Icons.Default.Check,
                        contentDescription = null,
                        tint = DieselOnPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isSyncing) "PUSHING ROW #${currentDay + 2}..." else "SAVE & PUSH TO SHEET1 (ROW ${currentDay + 2})",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        ),
                        color = DieselOnPrimary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    if (showQuickRateDialog) {
        QuickRateEditDialog(
            initialRate = rate,
            dayNumber = currentDay,
            onDismiss = { showQuickRateDialog = false },
            onSaveRate = { newRate ->
                rate = newRate
                coroutineScope.launch {
                    val updated = logEntry.copy(colL_rate = newRate)
                    repository.saveOrUpdateLog(updated)
                }
            }
        )
    }

    if (showUnitManagementSheet) {
        UnitManagementBottomSheet(
            assets = assets,
            repository = repository,
            onDismiss = { showUnitManagementSheet = false }
        )
    }
}

@Composable
fun DynamicFleetUnitCard(
    col: String,
    unitNumber: String,
    plate: String,
    name: String,
    vehicleType: String,
    meter: String,
    value: String,
    onValueChange: (String) -> Unit,
    isIdle: Boolean = false,
    readOnly: Boolean = false
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isIdle) DieselSurfaceContainerLowest.copy(alpha = 0.7f) else DieselSurfaceContainerLowest
        ),
        shape = RoundedCornerShape(10.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(DieselSurfaceContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = col,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                            color = DieselSecondary
                        )
                        Text(
                            text = unitNumber,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (isIdle) DieselSecondary else DieselPrimary
                        )
                    }
                }

                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = plate,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                textDecoration = if (isIdle) TextDecoration.LineThrough else null
                            ),
                            color = if (isIdle) DieselSecondary else DieselOnSurface
                        )

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (isIdle) DieselSurfaceContainerHighest else DieselSurfaceContainer)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = name,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 10.sp,
                                    fontWeight = if (isIdle) FontWeight.Bold else FontWeight.Normal
                                ),
                                color = DieselSecondary
                            )
                        }
                    }

                    Text(
                        text = "$vehicleType • $meter",
                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace, fontSize = 11.sp),
                        color = DieselSecondary
                    )
                }
            }

            Box(
                modifier = Modifier
                    .width(84.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isIdle) DieselSurfaceContainer else DieselSurfaceContainerLow)
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    BasicTextField(
                        value = value,
                        onValueChange = onValueChange,
                        readOnly = isIdle || readOnly,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        textStyle = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            textAlign = TextAlign.End,
                            color = if (isIdle) DieselSecondary else DieselOnSurface
                        ),
                        cursorBrush = SolidColor(DieselPrimary),
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "L",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = DieselSecondary
                    )
                }
            }
        }
    }
}
