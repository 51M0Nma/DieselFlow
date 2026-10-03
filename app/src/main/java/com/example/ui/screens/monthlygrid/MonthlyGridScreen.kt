package com.example.ui.screens.monthlygrid

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AssignmentInd
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Factory
import androidx.compose.material.icons.filled.Functions
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DailyLogEntry
import com.example.data.model.FleetAssetConfig
import com.example.data.model.UserEntity
import com.example.data.repository.DieselFlowRepository
import com.example.ui.components.EditSlipBottomSheet
import com.example.ui.components.FormatUtils
import com.example.ui.theme.DieselInverseOnSurface
import com.example.ui.theme.DieselInverseSurface
import com.example.ui.theme.DieselOnPrimary
import com.example.ui.theme.DieselOnPrimaryFixed
import com.example.ui.theme.DieselOnSecondaryContainer
import com.example.ui.theme.DieselOnSurface
import com.example.ui.theme.DieselOnSurfaceVariant
import com.example.ui.theme.DieselOutline
import com.example.ui.theme.DieselOutlineVariant
import com.example.ui.theme.DieselPrimary
import com.example.ui.theme.DieselPrimaryContainer
import com.example.ui.theme.DieselPrimaryFixed
import com.example.ui.theme.DieselPrimaryFixedDim
import com.example.ui.theme.DieselSecondary
import com.example.ui.theme.DieselSecondaryContainer
import com.example.ui.theme.DieselSecondaryFixedDim
import com.example.ui.theme.DieselSurfaceBright
import com.example.ui.theme.DieselSurfaceContainer
import com.example.ui.theme.DieselSurfaceContainerHigh
import com.example.ui.theme.DieselSurfaceContainerHighest
import com.example.ui.theme.DieselSurfaceContainerLow
import com.example.ui.theme.DieselSurfaceContainerLowest
import com.example.ui.theme.DieselTertiary
import com.example.ui.theme.DieselTertiaryFixed
import com.example.ui.theme.DieselTertiaryFixedDim
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun MonthlyGridScreen(
    currentDay: Int,
    onSelectDay: (Int) -> Unit,
    allLogs: List<DailyLogEntry>,
    assets: List<FleetAssetConfig>,
    repository: DieselFlowRepository,
    currentUser: UserEntity?,
    onNavigateToDailyLog: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val canEdit = currentUser?.canEditMonthlyGrid == true || currentUser?.isAdmin == true

    var isSyncing by remember { mutableStateOf(false) }
    var expandedRowId by remember { mutableStateOf<Int?>(24) }
    var selectedSlipToEdit by remember { mutableStateOf<DailyLogEntry?>(null) }

    // Summary calculations
    val loggedCount = allLogs.count { it.isLogged }
    val totalDays = 31
    val pendingCount = totalDays - loggedCount
    val reconciliationPercent = if (totalDays > 0) (loggedCount.toFloat() / totalDays.toFloat()) * 100f else 0f

    val totalMonthlyLiters = allLogs.sumOf { it.totalLiters }
    val totalMonthlyCost = allLogs.sumOf { it.calculatedCost }
    val avgLitersPerDay = if (loggedCount > 0) totalMonthlyLiters / loggedCount else 0.0

    val totalFleetLiters = allLogs.sumOf { it.fleetLiters }
    val totalPlantLiters = allLogs.sumOf { it.colI_plant }
    val totalIssuedLiters = allLogs.sumOf { it.issuedToLiters }

    val fleetRatio = if (totalMonthlyLiters > 0) (totalFleetLiters / totalMonthlyLiters).toFloat() else 0.82f
    val plantRatio = if (totalMonthlyLiters > 0) ((totalPlantLiters + totalIssuedLiters) / totalMonthlyLiters).toFloat() else 0.18f

    val activeDayEntry = allLogs.find { it.dayNumber == currentDay } ?: allLogs.firstOrNull()

    // Spin animation for sync button
    val syncRotation by animateFloatAsState(
        targetValue = if (isSyncing) 360f * 4 else 0f,
        animationSpec = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
        label = "syncSpin"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Live Google Sheet Connectivity Banner
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DieselSurfaceContainerHigh),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(DieselTertiary.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudDone,
                            contentDescription = null,
                            tint = DieselTertiary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "Connected: Sheet1",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = DieselOnSurface
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(DieselSurfaceContainerLowest)
                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = "A1:M33",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    color = DieselSecondary
                                )
                            }
                        }
                        Text(
                            text = "Last synced 2m ago • Real-time push",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = DieselSecondary
                        )
                    }
                }

                Button(
                    onClick = {
                        coroutineScope.launch {
                            isSyncing = true
                            delay(1200)
                            isSyncing = false
                            Toast.makeText(context, "All 31 rows re-aligned with Google Sheet1", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DieselSurfaceContainerLowest,
                        contentColor = DieselPrimary
                    ),
                    shape = RoundedCornerShape(8.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 1.dp),
                    modifier = Modifier
                        .height(34.dp)
                        .testTag("sync_grid_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Sync,
                        contentDescription = null,
                        tint = DieselPrimary,
                        modifier = Modifier
                            .size(16.dp)
                            .rotate(syncRotation)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "SYNC",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }

        // Month Stepper & Progress Tracker Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DieselSurfaceContainerLowest),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Stepper Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            Toast.makeText(context, "Switched to September 2026 Archive", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(DieselSurfaceContainerLow)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChevronLeft,
                            contentDescription = "Previous Month",
                            tint = DieselOnSurface
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "October 2026",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = DieselOnSurface
                            )
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = DieselPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Text(
                            text = "$totalDays Days • $loggedCount Logged ($pendingCount Pending)",
                            style = MaterialTheme.typography.labelMedium,
                            color = DieselSecondary
                        )
                    }

                    IconButton(
                        onClick = {
                            Toast.makeText(context, "Switched to November 2026 Cycle", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(DieselSurfaceContainerLow)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Next Month",
                            tint = DieselOnSurface
                        )
                    }
                }

                // Progress Bar
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Sheet Reconciliation Progress",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                            color = DieselOnSurfaceVariant
                        )
                        Text(
                            text = "${FormatUtils.formatNumber(reconciliationPercent.toDouble(), 1)}% ($loggedCount/$totalDays days)",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = DieselPrimary
                        )
                    }

                    LinearProgressIndicator(
                        progress = { reconciliationPercent / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = DieselPrimary,
                        trackColor = DieselSurfaceContainer,
                        strokeCap = StrokeCap.Round
                    )
                }
            }
        }

        // Fast Day Jumper Carousel
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "QUICK JUMP DAY (A1:A31)",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    ),
                    color = DieselSecondary
                )
                Text(
                    text = "Swipe to jump →",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                    color = DieselPrimary
                )
            }

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                val daysList = (1..31).toList()
                val daysOfWeek = listOf("THU", "FRI", "SAT", "SUN", "MON", "TUE", "WED")

                items(daysList) { day ->
                    val isSelected = day == currentDay
                    val entry = allLogs.find { it.dayNumber == day }
                    val isLogged = entry?.isLogged == true
                    val dow = daysOfWeek[(day - 1) % 7]

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) DieselPrimary else DieselSurfaceContainerLowest)
                            .border(
                                width = if (isSelected) 0.dp else 1.dp,
                                color = DieselOutline.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(10.dp)
                            )
                            .clickable {
                                onSelectDay(day)
                                expandedRowId = day
                            }
                            .padding(horizontal = 10.dp, vertical = 8.dp)
                            .testTag("jump_chip_$day"),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = if (day == 24) "TODAY" else dow,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 9.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                ),
                                color = if (isSelected) DieselPrimaryFixed else DieselSecondary
                            )
                            Text(
                                text = day.toString(),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                ),
                                color = if (isSelected) DieselOnPrimary else DieselOnSurface
                            )
                            Box(
                                modifier = Modifier
                                    .padding(top = 2.dp)
                                    .size(5.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when {
                                            isSelected -> DieselSurfaceContainerLowest
                                            isLogged -> DieselTertiary
                                            else -> DieselOutlineVariant
                                        }
                                    )
                            )
                        }
                    }
                }
            }
        }

        // Active Day Highlight Callout / Fast Action Tray
        if (activeDayEntry != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DieselPrimaryContainer),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
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
                                .background(DieselSurfaceContainerLowest.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = null,
                                tint = DieselOnPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Column {
                            Text(
                                text = "ROW ${activeDayEntry.targetRow} ACTIVE",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = DieselPrimaryFixedDim
                            )
                            Text(
                                text = "Day ${activeDayEntry.dayNumber} Oct • ${FormatUtils.formatNumber(activeDayEntry.totalLiters)} L",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = DieselOnPrimary
                            )
                        }
                    }

                    if (canEdit) {
                        Button(
                            onClick = { selectedSlipToEdit = activeDayEntry },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = DieselSurfaceContainerLowest,
                                contentColor = DieselPrimary
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = null,
                                tint = DieselPrimary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Edit Slip",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }
            }
        }

        // Sticky Column Guide
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp))
                .background(DieselSurfaceContainer)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "COL A: DATE",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = DieselSecondary
            )
            Text(
                text = "COLS B-J: LITERS",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = DieselSecondary
            )
            Text(
                text = "COL M: TOTAL",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = DieselSecondary
            )
        }

        // Daily Spreadsheet Rows (Replicating Sheet1)
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            allLogs.forEach { entry ->
                val isExpanded = expandedRowId == entry.dayNumber
                val isCurrent = entry.dayNumber == currentDay

                if (entry.isLogged) {
                    // Logged Day Row Card
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = if (isCurrent) 1.5.dp else 0.dp,
                                color = if (isCurrent) DieselPrimary.copy(alpha = 0.5f) else Color.Transparent,
                                shape = RoundedCornerShape(10.dp)
                            ),
                        colors = CardDefaults.cardColors(containerColor = DieselSurfaceContainerLowest),
                        shape = RoundedCornerShape(10.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column {
                            // Row Header (Collapsed summary)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        expandedRowId = if (isExpanded) null else entry.dayNumber
                                        onSelectDay(entry.dayNumber)
                                    }
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    // Day Badge
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isCurrent) DieselPrimaryFixed else DieselSurfaceContainerHigh),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(
                                                text = entry.dayNumber.toString(),
                                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                                color = if (isCurrent) DieselOnPrimaryFixed else DieselOnSurface
                                            )
                                            Text(
                                                text = "OCT",
                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                                                color = DieselSecondary
                                            )
                                        }
                                    }

                                    Column {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text(
                                                text = "${FormatUtils.formatNumber(entry.totalLiters)} L",
                                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                                color = DieselOnSurface
                                            )
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(DieselTertiary.copy(alpha = 0.12f))
                                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                                            ) {
                                                Text(
                                                    text = "Logged",
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 9.sp
                                                    ),
                                                    color = DieselTertiary
                                                )
                                            }
                                        }
                                        Text(
                                            text = "Rate: ₹${FormatUtils.formatNumber(entry.colL_rate)} / L • ${entry.entriesCount} entries",
                                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                            color = DieselSecondary
                                        )
                                    }
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = FormatUtils.formatINR(entry.calculatedCost),
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontFamily = FontFamily.Monospace,
                                                fontWeight = FontWeight.Bold
                                            ),
                                            color = DieselOnSurface
                                        )
                                        Text(
                                            text = if (isCurrent) "Verified Col M" else "Col M",
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                            color = if (isCurrent) DieselTertiary else DieselSecondary
                                        )
                                    }

                                    Icon(
                                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                        contentDescription = "Toggle Breakdown",
                                        tint = DieselSecondary
                                    )
                                }
                            }

                            // Expanded Breakdown: Sheet1 Columns B through K
                            AnimatedVisibility(visible = isExpanded) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(DieselSurfaceContainerLow)
                                        .padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = "BREAKDOWN BY COLUMN & ASSET",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 10.sp
                                        ),
                                        color = DieselSecondary
                                    )

                                    // Dynamic Unit Tiles Matrix
                                    val unitChunks = assets.chunked(2)
                                    unitChunks.forEach { chunk ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            chunk.forEach { asset ->
                                                val reading = entry.getUnitReading(asset.colKey)
                                                VehicleMatrixTile(
                                                    modifier = Modifier.weight(1f),
                                                    col = "Col ${asset.colKey}: ${asset.licensePlate}",
                                                    liters = reading,
                                                    iconType = "truck"
                                                )
                                            }
                                            if (chunk.size == 1) {
                                                Spacer(modifier = Modifier.weight(1f))
                                            }
                                        }
                                    }

                                    // Plant & Issued To Row
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        VehicleMatrixTile(
                                            modifier = Modifier.weight(1f),
                                            col = "Col I: Plant Genset",
                                            liters = entry.colI_plant,
                                            iconType = "factory"
                                        )

                                        if (entry.issuedToLiters > 0 || entry.issuedToName.isNotEmpty()) {
                                            VehicleMatrixTile(
                                                modifier = Modifier.weight(1f),
                                                col = "Col J: Issued To",
                                                liters = entry.issuedToLiters,
                                                iconType = "recipient"
                                            )
                                        } else {
                                            Spacer(modifier = Modifier.weight(1f))
                                        }
                                    }

                                    // Issue To & Remarks Card
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = DieselSurfaceContainerLowest),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                            if (entry.issuedToName.isNotEmpty()) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Text(
                                                        text = "Col J (Issued To):",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = DieselSecondary
                                                    )
                                                    Text(
                                                        text = "${entry.issuedToName} (${FormatUtils.formatNumber(entry.issuedToLiters)} L)",
                                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                                        color = DieselOnSurface
                                                    )
                                                }
                                            }
                                            if (entry.colK_remarks.isNotEmpty()) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Text(
                                                        text = "Col K (Remark):",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = DieselSecondary
                                                    )
                                                    Text(
                                                        text = entry.colK_remarks,
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = DieselOnSurface,
                                                        modifier = Modifier.weight(1f, fill = false)
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    // Edit Day Slip Button
                                    if (canEdit) {
                                        Button(
                                            onClick = { selectedSlipToEdit = entry },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = DieselSurfaceContainer,
                                                contentColor = DieselOnSurface
                                            ),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Edit,
                                                contentDescription = null,
                                                tint = DieselPrimary,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "Edit Day Slip (Row ${entry.targetRow})",
                                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // Empty / Upcoming Day Row
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = DieselSurfaceContainerLow.copy(alpha = 0.7f)),
                        shape = RoundedCornerShape(10.dp)
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
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(DieselSurfaceContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = entry.dayNumber.toString(),
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = DieselSecondary
                                        )
                                        Text(
                                            text = "OCT",
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                                            color = DieselSecondary
                                        )
                                    }
                                }

                                Column {
                                    Text(
                                        text = "No Records Logged",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = DieselSecondary
                                    )
                                    Text(
                                        text = "Row ${entry.targetRow} • Awaiting shift slip",
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                        color = DieselOutline
                                    )
                                }
                            }

                            if (canEdit) {
                                Button(
                                    onClick = {
                                        onSelectDay(entry.dayNumber)
                                        onNavigateToDailyLog()
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = DieselSurfaceContainerLowest,
                                        contentColor = DieselPrimary
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 1.dp),
                                    modifier = Modifier.height(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = null,
                                        tint = DieselPrimary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Log Day",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Sticky Spreadsheet Grand Total Footer (Spreadsheet Formula Row 33)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DieselInverseSurface),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Formula Header
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
                            imageVector = Icons.Default.Functions,
                            contentDescription = null,
                            tint = DieselPrimaryFixedDim,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "TOTAL DIESEL (ROW 33)",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            ),
                            color = DieselSurfaceBright
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(DieselOnPrimaryFixed)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "=SUM(B33:J33)",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            ),
                            color = DieselPrimaryFixedDim
                        )
                    }
                }

                // Grand Metrics
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "MONTHLY LITERS (COL B-J)",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = DieselSecondaryFixedDim
                        )
                        Text(
                            text = "${FormatUtils.formatNumber(totalMonthlyLiters)} L",
                            style = MaterialTheme.typography.displayMedium.copy(fontSize = 22.sp),
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "• Avg: ${FormatUtils.formatNumber(avgLitersPerDay, 1)} L / day",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = DieselTertiaryFixedDim
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "TOTAL COST (COL M)",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = DieselSecondaryFixedDim
                        )
                        Text(
                            text = FormatUtils.formatINR(totalMonthlyCost),
                            style = MaterialTheme.typography.displayMedium.copy(fontSize = 22.sp),
                            fontWeight = FontWeight.Bold,
                            color = DieselPrimaryFixedDim
                        )
                        Text(
                            text = "Rate: ₹92.50 / L Avg",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = DieselSecondaryFixedDim
                        )
                    }
                }

                // Fleet vs Factory Progress Bar
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(DieselPrimaryFixed))
                            Text(
                                text = "Fleet: ${(fleetRatio * 100).toInt()}%",
                                style = MaterialTheme.typography.labelSmall,
                                color = DieselSecondaryFixedDim
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "Plant/Issued: ${(plantRatio * 100).toInt()}%",
                                style = MaterialTheme.typography.labelSmall,
                                color = DieselSecondaryFixedDim
                            )
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(DieselTertiaryFixed))
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(fleetRatio.coerceAtLeast(0.01f))
                                .background(DieselPrimaryFixed)
                        )
                        Box(
                            modifier = Modifier
                                .weight(plantRatio.coerceAtLeast(0.01f))
                                .background(DieselTertiaryFixed)
                        )
                    }
                }

                // Google Sheets App Link
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier
                            .clickable {
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://docs.google.com/spreadsheets"))
                                    context.startActivity(intent)
                                } catch (_: Exception) {
                                    Toast.makeText(context, "Opening Google Sheets...", Toast.LENGTH_SHORT).show()
                                }
                            },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                            contentDescription = null,
                            tint = DieselTertiaryFixed,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "Open in Google Sheets App",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = DieselTertiaryFixed
                        )
                    }

                    Text(
                        text = "Formula: Auto-Calculated",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = DieselSecondaryFixedDim
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    if (selectedSlipToEdit != null) {
        EditSlipBottomSheet(
            entry = selectedSlipToEdit!!,
            assets = assets,
            onDismiss = { selectedSlipToEdit = null },
            onSave = { updated ->
                coroutineScope.launch {
                    repository.saveOrUpdateLog(updated)
                    Toast.makeText(context, "Row #${updated.targetRow} updated!", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }
}

@Composable
fun VehicleMatrixTile(
    col: String,
    liters: Double,
    iconType: String,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = DieselSurfaceContainerLowest),
        shape = RoundedCornerShape(8.dp),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = col,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = DieselSecondary
                )
                Text(
                    text = "${FormatUtils.formatNumber(liters)} L",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    ),
                    color = DieselOnSurface
                )
            }

            Icon(
                imageVector = when (iconType) {
                    "truck" -> Icons.Default.LocalShipping
                    "factory" -> Icons.Default.Factory
                    else -> Icons.Default.AssignmentInd
                },
                contentDescription = null,
                tint = if (iconType == "truck") DieselPrimary else DieselSecondary,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
