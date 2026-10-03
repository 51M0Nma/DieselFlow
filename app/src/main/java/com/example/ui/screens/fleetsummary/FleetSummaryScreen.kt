package com.example.ui.screens.fleetsummary

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.example.ui.components.ExportReportDialog
import com.example.ui.components.FormatUtils
import com.example.ui.theme.DieselInverseOnSurface
import com.example.ui.theme.DieselInverseSurface
import com.example.ui.theme.DieselOnPrimary
import com.example.ui.theme.DieselOnPrimaryFixed
import com.example.ui.theme.DieselOnSecondaryContainer
import com.example.ui.theme.DieselOnSurface
import com.example.ui.theme.DieselOnSurfaceVariant
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
import com.example.ui.theme.DieselSurfaceContainerLow
import com.example.ui.theme.DieselSurfaceContainerLowest
import com.example.ui.theme.DieselTertiary
import com.example.ui.theme.DieselTertiaryContainer
import com.example.ui.theme.DieselTertiaryFixedDim

data class LeaderboardItem(
    val unitNumber: Int,
    val colKey: String,
    val plate: String,
    val name: String,
    val description: String,
    val totalLiters: Double,
    val estimatedCost: Double,
    val isYardStandby: Boolean = false
)

@Composable
fun FleetSummaryScreen(
    allLogs: List<DailyLogEntry>,
    assets: List<FleetAssetConfig>,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showExportDialog by remember { mutableStateOf(false) }
    var isSortAscending by remember { mutableStateOf(false) }
    val expandedCards = remember { mutableStateMapOf<Int, Boolean>() }

    // Dynamic real aggregations strictly from allLogs
    val totalMonthlyLiters = allLogs.sumOf { it.totalLiters }
    val totalMonthlyCost = allLogs.sumOf { it.calculatedCost }
    val totalFleetLiters = allLogs.sumOf { it.fleetLiters }
    val totalPlantLiters = allLogs.sumOf { it.colI_plant }
    val loggedDaysCount = allLogs.count { it.isLogged }

    val fleetCostEst = totalFleetLiters * 92.50
    val plantCostEst = totalPlantLiters * 92.50

    val fleetPercent = if (totalMonthlyLiters > 0) ((totalFleetLiters / totalMonthlyLiters) * 100).toInt() else 0
    val plantPercent = if (totalMonthlyLiters > 0) ((totalPlantLiters / totalMonthlyLiters) * 100).toInt() else 0

    // Department SUMIF allocations
    val logisticsSum = allLogs.filter { it.colJ_issueTo.contains("Logistics", ignoreCase = true) }.sumOf { it.totalLiters }
    val earthworksSum = allLogs.filter { it.colJ_issueTo.contains("Earthworks", ignoreCase = true) }.sumOf { it.totalLiters }
    val plantBackupSum = totalPlantLiters

    // Vehicle specific sums
    val assetMap = assets.associateBy { it.colKey }

    val u1Sum = allLogs.sumOf { it.colB_unit1 }
    val u2Sum = allLogs.sumOf { it.colC_unit2 }
    val u3Sum = allLogs.sumOf { it.colD_unit3 }
    val u4Sum = allLogs.sumOf { it.colE_unit4 }
    val u5Sum = allLogs.sumOf { it.colF_unit5 }
    val u6Sum = allLogs.sumOf { it.colG_unit6 }
    val u7Sum = allLogs.sumOf { it.colH_unit7 }

    val rawLeaderboard = listOf(
        LeaderboardItem(6, "G", assetMap["G"]?.licensePlate ?: "GA 3345", "Unit 6", "Tanker 18kL • Col G", u6Sum, u6Sum * 92.50),
        LeaderboardItem(2, "C", assetMap["C"]?.licensePlate ?: "GA 2332", "Unit 2", "30T Rigid • Col C", u2Sum, u2Sum * 92.50),
        LeaderboardItem(1, "B", assetMap["B"]?.licensePlate ?: "GA 2352", "Unit 1", "40T Hauler • Col B", u1Sum, u1Sum * 92.50),
        LeaderboardItem(4, "E", assetMap["E"]?.licensePlate ?: "GA 8812", "Unit 4", "Tipper Dump • Col E", u4Sum, u4Sum * 92.50),
        LeaderboardItem(3, "D", assetMap["D"]?.licensePlate ?: "GA 4109", "Unit 3", "Flatbed Semi • Col D", u3Sum, u3Sum * 92.50),
        LeaderboardItem(7, "H", assetMap["H"]?.licensePlate ?: "GA 5520", "Unit 7", "Depot Loader • Col H", u7Sum, u7Sum * 92.50),
        LeaderboardItem(5, "F", assetMap["F"]?.licensePlate ?: "GA 9011", "Unit 5", "Heavy Crane • Col F", u5Sum, u5Sum * 92.50, isYardStandby = true)
    )

    val leaderboard = if (isSortAscending) {
        rawLeaderboard.sortedBy { it.totalLiters }
    } else {
        rawLeaderboard.sortedByDescending { it.totalLiters }
    }

    val maxVolume = leaderboard.maxOfOrNull { it.totalLiters }?.takeIf { it > 0 } ?: 1.0

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header Banner & Period Controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(DieselSecondaryContainer)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (loggedDaysCount >= 31) "RECONCILIATION CLOSED" else "RECONCILIATION IN PROGRESS",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp
                        ),
                        color = DieselOnSecondaryContainer
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(modifier = Modifier.size(5.dp).clip(CircleShape).background(DieselTertiary))
                    Text(
                        text = "Formulas Verified",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                        color = DieselTertiary
                    )
                }
            }

            Button(
                onClick = { showExportDialog = true },
                colors = ButtonDefaults.buttonColors(
                    containerColor = DieselSurfaceContainerHigh,
                    contentColor = DieselOnSurface
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .height(34.dp)
                    .testTag("export_csv_pdf_button")
            ) {
                Icon(
                    imageVector = Icons.Default.FileDownload,
                    contentDescription = null,
                    tint = DieselPrimary,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Export CSV/PDF",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                )
            }
        }

        // Title and subtitle
        Column {
            Text(
                text = "October 2026 Fleet Reconciliation",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = DieselOnSurface
            )
            Text(
                text = "Audited bulk metered dispensing summary & fleet sub-ledger allocation",
                style = MaterialTheme.typography.bodySmall,
                color = DieselOnSurfaceVariant
            )
        }

        // Grand Totals Hero Card (Bento Hero)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DieselInverseSurface),
            shape = RoundedCornerShape(14.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column {
                        Text(
                            text = "TOTAL DIESEL CONSUMED",
                            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.5.sp),
                            color = DieselSecondaryFixedDim
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = FormatUtils.formatNumber(totalMonthlyLiters, 0),
                                style = MaterialTheme.typography.displayLarge.copy(fontSize = 32.sp),
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "LITERS",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                color = DieselPrimaryFixedDim,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(DieselInverseOnSurface.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalGasStation,
                            contentDescription = null,
                            tint = DieselPrimaryFixedDim,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                // Gross fuel expense pill
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(DieselInverseOnSurface.copy(alpha = 0.08f))
                        .padding(horizontal = 12.dp, vertical = 8.dp)
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
                                imageVector = Icons.Default.Payments,
                                contentDescription = null,
                                tint = DieselTertiaryFixedDim,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Gross Fuel Expense",
                                style = MaterialTheme.typography.labelSmall,
                                color = DieselSecondaryFixedDim
                            )
                        }

                        Text(
                            text = FormatUtils.formatINR(totalMonthlyCost),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            ),
                            color = Color.White
                        )
                    }
                }
            }
        }

        // Fleet vs Factory Share Bento Tiles
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Fleet Vehicles Share (Col B-H)
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = DieselSurfaceContainerLowest),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "FLEET (COLS B-H)",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = DieselSecondary
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(DieselTertiary.copy(alpha = 0.12f))
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = "$fleetPercent%",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                ),
                                color = DieselTertiary
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = FormatUtils.formatNumber(totalFleetLiters, 0),
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = DieselOnSurface
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "L",
                            style = MaterialTheme.typography.labelSmall,
                            color = DieselSecondary,
                            modifier = Modifier.padding(bottom = 2.dp)
                        )
                    }

                    Text(
                        text = "${FormatUtils.formatINR(fleetCostEst)} est.",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = DieselSecondary
                    )

                    LinearProgressIndicator(
                        progress = { if (totalMonthlyLiters > 0) (fleetPercent / 100f) else 0f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        color = DieselTertiary,
                        trackColor = DieselSurfaceContainer
                    )
                }
            }

            // Factory & Plant Share (Col I)
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = DieselSurfaceContainerLowest),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "FACTORY (COL I)",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = DieselSecondary
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(DieselSecondaryContainer)
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = "$plantPercent%",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                ),
                                color = DieselOnSecondaryContainer
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = FormatUtils.formatNumber(totalPlantLiters, 0),
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = DieselOnSurface
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "L",
                            style = MaterialTheme.typography.labelSmall,
                            color = DieselSecondary,
                            modifier = Modifier.padding(bottom = 2.dp)
                        )
                    }

                    Text(
                        text = "${FormatUtils.formatINR(plantCostEst)} est.",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = DieselSecondary
                    )

                    LinearProgressIndicator(
                        progress = { if (totalMonthlyLiters > 0) (plantPercent / 100f) else 0f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        color = DieselPrimary,
                        trackColor = DieselSurfaceContainer
                    )
                }
            }
        }

        // Department & Site Allocation (Col J Recipients)
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "PRIMARY COST ALLOCATIONS (COL J)",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = DieselSecondary
                )
                Text(
                    text = "Formula =SUMIF",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    ),
                    color = DieselPrimary
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AllocationPill(name = "Logistics Dept", liters = "${FormatUtils.formatNumber(logisticsSum, 0)} L", dotColor = DieselTertiary, modifier = Modifier.weight(1f))
                AllocationPill(name = "Earthworks Contractor", liters = "${FormatUtils.formatNumber(earthworksSum, 0)} L", dotColor = DieselPrimary, modifier = Modifier.weight(1f))
            }
            AllocationPill(name = "Plant Backup", liters = "${FormatUtils.formatNumber(plantBackupSum, 0)} L", dotColor = DieselSecondary, modifier = Modifier.fillMaxWidth(0.55f))
        }

        // Consumption Proportions & Telemetry Bar
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Consumption Proportions",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = DieselOnSurface
                        )
                        Text(
                            text = "Fleet logistics vs. static generation",
                            style = MaterialTheme.typography.bodySmall,
                            color = DieselSecondary
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.Analytics,
                        contentDescription = null,
                        tint = DieselSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Segmented Proportion Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(18.dp)
                        .clip(RoundedCornerShape(4.dp))
                ) {
                    Box(
                        modifier = Modifier
                            .weight(if (fleetPercent > 0) fleetPercent.toFloat() else 0.5f)
                            .background(DieselTertiary)
                    )
                    Box(
                        modifier = Modifier
                            .weight(if (plantPercent > 0) plantPercent.toFloat() else 0.5f)
                            .background(DieselPrimaryContainer)
                    )
                }

                // Legend
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Box(modifier = Modifier.size(8.dp).clip(RoundedCornerShape(2.dp)).background(DieselTertiary))
                        Text(
                            text = "Rolling Fleet: ${FormatUtils.formatNumber(totalFleetLiters, 0)} L ($fleetPercent%)",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                            color = DieselOnSurface
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Box(modifier = Modifier.size(8.dp).clip(RoundedCornerShape(2.dp)).background(DieselPrimaryContainer))
                        Text(
                            text = "Gensets & Kiln: ${FormatUtils.formatNumber(totalPlantLiters, 0)} L ($plantPercent%)",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                            color = DieselOnSurface
                        )
                    }
                }

                // Quick Telemetry Snapshot Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(DieselSurfaceContainerLow)
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    val dailyAvg = if (loggedDaysCount > 0) totalMonthlyLiters / loggedDaysCount else 0.0
                    TelemetryItem(label = "Fuel Benchmark", value = "₹92.50 / L")
                    TelemetryItem(label = "Operating Days", value = "$loggedDaysCount Days")
                    TelemetryItem(label = "Daily Avg Run", value = "${FormatUtils.formatNumber(dailyAvg, 1)} L/d")
                }
            }
        }

        // Vehicle Consumption Leaderboard (Cols B-H)
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Vehicle Leaderboard (Cols B–H)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = DieselOnSurface
                    )
                    Text(
                        text = "Ranked by volume • Tap card for daily metrics",
                        style = MaterialTheme.typography.bodySmall,
                        color = DieselSecondary
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(DieselSurfaceContainerLow)
                        .clickable { isSortAscending = !isSortAscending }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Sort,
                            contentDescription = null,
                            tint = DieselOnSurfaceVariant,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = if (isSortAscending) "Low-to-High" else "High-to-Low",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = DieselOnSurfaceVariant
                        )
                    }
                }
            }

            // Cards List
            leaderboard.forEachIndexed { index, item ->
                val isExpanded = expandedCards[item.unitNumber] == true
                val progressRatio = if (item.totalLiters > 0) (item.totalLiters / maxVolume).toFloat().coerceIn(0.05f, 1f) else 0f
                val rank = if (isSortAscending) (7 - index) else (index + 1)

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            expandedCards[item.unitNumber] = !isExpanded
                        }
                        .testTag("vehicle_card_unit_${item.unitNumber}"),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isExpanded) DieselSurfaceContainerLow else DieselSurfaceContainerLowest
                    ),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                // Rank Circle Badge
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(
                                            when (rank) {
                                                1 -> DieselPrimaryFixed
                                                else -> DieselSurfaceContainer
                                            }
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = rank.toString(),
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace
                                        ),
                                        color = when (rank) {
                                            1 -> DieselOnPrimaryFixed
                                            else -> DieselOnSurfaceVariant
                                        }
                                    )
                                }

                                Column {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = item.plate,
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = DieselOnSurface
                                        )
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(DieselSurfaceContainer)
                                                .padding(horizontal = 4.dp, vertical = 1.dp)
                                        ) {
                                            Text(
                                                text = item.name,
                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                                color = DieselSecondary
                                            )
                                        }

                                        if (item.isYardStandby) {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(DieselSecondaryContainer)
                                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                                            ) {
                                                Text(
                                                    text = "Yard standby",
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold
                                                    ),
                                                    color = DieselOnSecondaryContainer
                                                )
                                            }
                                        }
                                    }

                                    Text(
                                        text = item.description,
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                        color = DieselSecondary
                                    )
                                }
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "${FormatUtils.formatNumber(item.totalLiters, 0)} L",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = DieselOnSurface
                                )
                                Text(
                                    text = FormatUtils.formatINR(item.estimatedCost),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    ),
                                    color = DieselPrimary
                                )
                            }
                        }

                        // Progress Bar
                        LinearProgressIndicator(
                            progress = { progressRatio },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(5.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = if (item.isYardStandby) DieselSecondary else DieselPrimary,
                            trackColor = DieselSurfaceContainer,
                            strokeCap = StrokeCap.Round
                        )

                        // Expandable Detail Pane
                        AnimatedVisibility(visible = isExpanded) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(DieselSurfaceContainerLowest)
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceAround
                            ) {
                                val daysDiv = if (loggedDaysCount > 0) loggedDaysCount.toDouble() else 31.0
                                val avgDailyUsage = item.totalLiters / daysDiv
                                val avgDailyCost = item.estimatedCost / daysDiv

                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "Avg Daily Usage",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = DieselSecondary
                                    )
                                    Text(
                                        text = "${FormatUtils.formatNumber(avgDailyUsage, 1)} L/day",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = DieselOnSurface
                                    )
                                }

                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "Avg Daily Cost",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = DieselSecondary
                                    )
                                    Text(
                                        text = "${FormatUtils.formatINR(avgDailyCost)}/day",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = DieselOnSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    if (showExportDialog) {
        ExportReportDialog(
            logs = allLogs,
            onDismiss = { showExportDialog = false }
        )
    }
}

@Composable
fun AllocationPill(
    name: String,
    liters: String,
    dotColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = DieselSurfaceContainerLowest),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(dotColor))
                Text(
                    text = name,
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                    color = DieselOnSurface
                )
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(DieselSurfaceContainer)
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = liters,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp
                    ),
                    color = DieselSecondary
                )
            }
        }
    }
}

@Composable
fun TelemetryItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
            color = DieselSecondary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            color = DieselOnSurface
        )
    }
}
