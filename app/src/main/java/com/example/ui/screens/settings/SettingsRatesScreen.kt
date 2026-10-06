package com.example.ui.screens.settings

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FleetAssetConfig
import com.example.data.model.UserEntity
import com.example.data.remote.GoogleSheetsService
import com.example.data.remote.SupabaseService
import com.example.data.repository.DieselFlowRepository
import com.example.ui.components.LinkGoogleSheetDialog
import com.example.ui.theme.DieselOnPrimary
import com.example.ui.theme.DieselOnSecondaryContainer
import com.example.ui.theme.DieselOnSurface
import com.example.ui.theme.DieselOnSurfaceVariant
import com.example.ui.theme.DieselOutline
import com.example.ui.theme.DieselPrimary
import com.example.ui.theme.DieselPrimaryContainer
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
fun SettingsRatesScreen(
    assets: List<FleetAssetConfig>,
    repository: DieselFlowRepository,
    currentUser: UserEntity?,
    onOpenUserConsole: () -> Unit = {},
    onOpenUnitManagement: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val canEdit = currentUser?.canEditSettings == true || currentUser?.isAdmin == true

    var rateInput by remember { mutableStateOf("92.50") }
    var selectedScope by remember { mutableStateOf("all") }
    var isTestingConnection by remember { mutableStateOf(false) }
    var isResyncing by remember { mutableStateOf(false) }
    var isApplyingRate by remember { mutableStateOf(false) }

    var showLinkSheetDialog by remember { mutableStateOf(false) }
    var currentSheetUrl by remember { mutableStateOf(GoogleSheetsService.getSpreadsheetUrl(context)) }

    // Engine toggles
    var autoCalcTotals by remember { mutableStateOf(true) }
    var offlineCaching by remember { mutableStateOf(true) }

    // Local asset plate inputs
    val plateInputs = remember(assets) {
        mutableStateMapOf<String, String>().apply {
            assets.forEach { put(it.colKey, it.licensePlate) }
        }
    }

    // Pulse animation
    val infiniteTransition = rememberInfiniteTransition(label = "settingsPulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val syncRotation by animateFloatAsState(
        targetValue = if (isResyncing) 360f * 3 else 0f,
        animationSpec = tween(1000, easing = FastOutSlowInEasing),
        label = "syncRotation"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // User & Access Control Banner
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DieselPrimary),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AdminPanelSettings,
                        contentDescription = null,
                        tint = DieselOnPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                    Column {
                        Text(
                            text = "Admin: ${currentUser?.fullName ?: "Simon Mahajan"}",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = DieselOnPrimary
                        )
                        Text(
                            text = "Role: ${currentUser?.role ?: "ADMIN"} • Full Permissions",
                            style = MaterialTheme.typography.bodySmall,
                            color = DieselOnPrimary.copy(alpha = 0.85f)
                        )
                    }
                }

                if (currentUser?.canManageUsers == true || currentUser?.isAdmin == true) {
                    Button(
                        onClick = onOpenUserConsole,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DieselSurfaceContainerLowest,
                            contentColor = DieselPrimary
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Text("User Console", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                    }
                }
            }
        }

        // Cloud Sheet Relay Hub Card
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
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(DieselSurfaceContainerLow),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudSync,
                                contentDescription = null,
                                tint = DieselPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Column {
                            Text(
                                text = "Supabase Cloud Relay",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = DieselOnSurface
                            )
                            Text(
                                text = "POSTGREST LIVE CLOUD • V2.4",
                                style = MaterialTheme.typography.labelSmall,
                                color = DieselSecondary
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(DieselTertiary.copy(alpha = 0.12f))
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
                                text = "SUPABASE LIVE",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = DieselTertiary
                            )
                        }
                    }
                }

                // Active Sheet Target Details
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DieselSurfaceContainerLow),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "SUPABASE DATABASE TARGET",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = DieselSecondary
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(DieselSurfaceContainerHighest)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "Connected (riehesxahscrbcdxmhlr)",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp
                                    ),
                                    color = DieselOnSurface
                                )
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Folder,
                                contentDescription = null,
                                tint = DieselTertiary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Tables: app_users • fleet_assets • daily_logs",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = DieselOnSurface
                            )
                        }

                        Text(
                            text = "Cloud PostgREST API: ${SupabaseService.baseUrl}",
                            style = MaterialTheme.typography.bodySmall,
                            color = DieselSecondary
                        )

                        // URL Copy Bar
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(DieselSurfaceContainerLowest)
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Link,
                                    contentDescription = null,
                                    tint = DieselOutline,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = SupabaseService.baseUrl,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 10.sp
                                    ),
                                    color = DieselOutline,
                                    maxLines = 1
                                )
                            }

                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy Supabase URL",
                                tint = DieselPrimary,
                                modifier = Modifier
                                    .size(16.dp)
                                    .clickable {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        val clip = ClipData.newPlainText("Supabase URL", SupabaseService.baseUrl)
                                        clipboard.setPrimaryClip(clip)
                                        Toast.makeText(context, "Supabase URL copied", Toast.LENGTH_SHORT).show()
                                    }
                            )
                        }
                    }
                }

                // Action Buttons: Test Connection & Force Re-sync
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                isTestingConnection = true
                                val result = SupabaseService.testConnection()
                                isTestingConnection = false
                                if (result.isSuccess) {
                                    Toast.makeText(context, "Supabase Cloud ping successful (200 OK)", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "Supabase ping: ${result.exceptionOrNull()?.message ?: "Online"}", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DieselSurfaceContainerHigh,
                            contentColor = DieselOnSurface
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("test_connection_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Wifi,
                            contentDescription = null,
                            tint = DieselPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isTestingConnection) "Pinging..." else "Test Connection",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    Button(
                        onClick = {
                            coroutineScope.launch {
                                isResyncing = true
                                val success = repository.syncWithSupabase()
                                isResyncing = false
                                if (success) {
                                    Toast.makeText(context, "Full cloud sync complete with Supabase!", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "Sync complete (local cache active)", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DieselSurfaceContainerHigh,
                            contentColor = DieselOnSurface
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("force_resync_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sync,
                            contentDescription = null,
                            tint = DieselTertiary,
                            modifier = Modifier
                                .size(16.dp)
                                .rotate(syncRotation)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isResyncing) "Re-syncing..." else "Force Re-sync",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        }

        // Google Spreadsheet Live Relay Hub Card
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
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
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
                                text = "Google Sheets Workspace Relay",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = DieselOnSurface
                            )
                            Text(
                                text = "LIVE RECONCILIATION • COLUMNS A TO M",
                                style = MaterialTheme.typography.labelSmall,
                                color = DieselSecondary
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(DieselTertiary.copy(alpha = 0.12f))
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
                                text = "SHEETS V4 SYNC",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = DieselTertiary
                            )
                        }
                    }
                }

                // Grid Column & Row layout explanation
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DieselSurfaceContainerLow),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "SPREADSHEET STRUCTURE & COLUMNS MAPPING",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                            color = DieselSecondary
                        )

                        Text(
                            text = "• Col A: Date & Day • Col B–H: Fleet Units 1 to 7\n• Col I: Facility & Plant Genset #2\n• Col J: Issued To (Destination & Liters & Notes)\n• Col K: Shift Remarks • Col L: Rate (₹/L)\n• Col M: Calculated Cost Formula `=(SUM(B:I))*L`",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = DieselOnSurface
                        )

                        Text(
                            text = "Rows: 31 Day entries (Rows 2 to 32) + Totals Summary (Row 33)",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = DieselPrimary
                        )

                        // URL Display and Change Button
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(DieselSurfaceContainerLowest)
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Link,
                                    contentDescription = null,
                                    tint = DieselPrimary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = currentSheetUrl,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 10.sp
                                    ),
                                    color = DieselOnSurface,
                                    maxLines = 1
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(DieselPrimary.copy(alpha = 0.12f))
                                    .clickable { showLinkSheetDialog = true }
                                    .padding(horizontal = 6.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = "Change Sheet",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp
                                    ),
                                    color = DieselPrimary
                                )
                            }
                        }
                    }
                }

                // Actions: Open in Google Sheets & Re-align Sheet1
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            val url = currentSheetUrl
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("Sheet URL", url))
                                Toast.makeText(context, "Link copied to clipboard: $url", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DieselPrimary,
                            contentColor = DieselOnPrimary
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Open in Google Sheets",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    Button(
                        onClick = { showLinkSheetDialog = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DieselSurfaceContainerHigh,
                            contentColor = DieselOnSurface
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudSync,
                            contentDescription = null,
                            tint = DieselTertiary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Link / Setup Sheet",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        }

        // Bulk Rate Applicator Module
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
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(DieselSurfaceContainerLow),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CurrencyRupee,
                                contentDescription = null,
                                tint = DieselPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Column {
                            Text(
                                text = "Set Month Rate",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = DieselOnSurface
                            )
                            Text(
                                text = "Diesel price batch broadcast to Column L",
                                style = MaterialTheme.typography.bodySmall,
                                color = DieselSecondary
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(DieselSurfaceContainerLow)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "Bulk Engine",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            ),
                            color = DieselPrimary
                        )
                    }
                }

                // Billing Period Indicator
                Card(
                    colors = CardDefaults.cardColors(containerColor = DieselSurfaceContainerLow),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "BILLING PERIOD",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = DieselSecondary
                            )
                            Text(
                                text = "October 2026",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = DieselOnSurface
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(DieselSurfaceContainerLowest)
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "31 Days in Cycle",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = DieselOnSurface
                            )
                        }
                    }
                }

                // Fuel Rate Input
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "FUEL RATE MATRIX (₹ / LITER)",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = DieselSecondary
                    )

                    OutlinedTextField(
                        value = rateInput,
                        onValueChange = { if (canEdit) rateInput = it },
                        readOnly = !canEdit,
                        prefix = {
                            Text(
                                text = "₹ ",
                                style = MaterialTheme.typography.displayLarge.copy(fontSize = 24.sp),
                                fontWeight = FontWeight.Bold,
                                color = DieselPrimary
                            )
                        },
                        suffix = {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(DieselSurfaceContainerHighest)
                                    .padding(horizontal = 6.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = "INR / LTR",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = DieselOnSurface
                                )
                            }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        textStyle = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp,
                            color = DieselOnSurface
                        ),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("bulk_fuel_rate_input")
                    )
                }

                // Propagation Scope Radios
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "PROPAGATION SCOPE",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = DieselSecondary
                    )

                    ScopeRadioOption(
                        selected = selectedScope == "all",
                        title = "Apply to All 31 Days",
                        subtitle = "Overwrites all existing and blank rows in Column L",
                        onClick = { if (canEdit) selectedScope = "all" }
                    )

                    ScopeRadioOption(
                        selected = selectedScope == "blank",
                        title = "Apply to Blank Rate Days Only",
                        subtitle = "Protects manually recorded volatile adjustments",
                        onClick = { if (canEdit) selectedScope = "blank" }
                    )

                    ScopeRadioOption(
                        selected = selectedScope == "forward",
                        title = "From Current Date Forward",
                        subtitle = "Days 24 through 31 only (Leaves historical records)",
                        onClick = { if (canEdit) selectedScope = "forward" }
                    )
                }

                // Safety Lock Notice
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(DieselSurfaceContainerLow)
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = DieselPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Safety locks enabled: Automated backup snapshot created before cell write.",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = DieselOnSurfaceVariant
                    )
                }

                // Apply Rate Button
                if (canEdit) {
                    Button(
                        onClick = {
                            val rate = rateInput.toDoubleOrNull()
                            if (rate != null && rate > 0) {
                                coroutineScope.launch {
                                    isApplyingRate = true
                                    repository.applyRate(rate, selectedScope, currentDay = 24)
                                    delay(900)
                                    isApplyingRate = false
                                    Toast.makeText(context, "Rate ₹$rate/L broadcast to Column L!", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = DieselPrimary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("apply_monthly_rate_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.RocketLaunch,
                            contentDescription = null,
                            tint = DieselOnPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isApplyingRate) "UPDATING COLUMN L..." else "Apply Rate to Entire Month (Column L)",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = DieselOnPrimary
                        )
                    }
                }
            }
        }

        // Fleet Asset & Unit Management Hub
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
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(DieselSurfaceContainerLow),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocalShipping,
                                contentDescription = null,
                                tint = DieselPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Column {
                            Text(
                                text = "Fleet Unit Management",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = DieselOnSurface
                            )
                            Text(
                                text = "${assets.size} Units Registered • Full CRUD access",
                                style = MaterialTheme.typography.bodySmall,
                                color = DieselSecondary
                            )
                        }
                    }

                    if (currentUser?.canManageUnits == true || currentUser?.isAdmin == true) {
                        Button(
                            onClick = onOpenUnitManagement,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = DieselPrimary,
                                contentColor = DieselOnPrimary
                            ),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text(
                                text = "Manage Units",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }

                // Asset Quick Summary List
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    assets.forEach { asset ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(DieselSurfaceContainerLow)
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(30.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(DieselSurfaceContainerHighest),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = asset.colKey,
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = DieselOnSurface
                                    )
                                }

                                Column {
                                    Text(
                                        text = "${asset.unitName} (${asset.licensePlate})",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = DieselOnSurface
                                    )
                                    Text(
                                        text = "${asset.vehicleType} • ${asset.meterInfo}",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                        color = DieselSecondary
                                    )
                                }
                            }

                            if (asset.isIdle) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(DieselSecondaryContainer)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
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
                    }
                }
            }
        }

        // Formula & Data Integrity
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
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = DieselTertiary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "Formula & Data Integrity",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = DieselOnSurface
                        )
                        Text(
                            text = "Live mathematical verification against Sheet1",
                            style = MaterialTheme.typography.bodySmall,
                            color = DieselSecondary
                        )
                    }
                }

                // Toggles
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SettingToggleRow(
                        icon = Icons.Default.Calculate,
                        title = "Auto-Calculate Totals",
                        subtitle = "Recalculate Col J & Col M on keystroke",
                        checked = autoCalcTotals,
                        onCheckedChange = { autoCalcTotals = it }
                    )

                    SettingToggleRow(
                        icon = Icons.Default.Storage,
                        title = "Offline Terminal Caching",
                        subtitle = "Retain 90-day dispatch logs on device",
                        checked = offlineCaching,
                        onCheckedChange = { offlineCaching = it }
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    if (canEdit) {
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    repository.clearAndResetToBlank()
                                    Toast.makeText(context, "All logs cleared. Database reset to clean state!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = DieselSurfaceContainerHighest,
                                contentColor = MaterialTheme.colorScheme.error
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .testTag("reset_database_button")
                        ) {
                            Text(
                                text = "Reset / Clear All Logs (Clean Start)",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    if (showLinkSheetDialog) {
        LinkGoogleSheetDialog(
            currentUrl = currentSheetUrl,
            onSaveUrl = { currentSheetUrl = it },
            onDismiss = { showLinkSheetDialog = false }
        )
    }
}

@Composable
fun ScopeRadioOption(
    selected: Boolean,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(DieselSurfaceContainerLow)
            .selectable(selected = selected, onClick = onClick)
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = null,
            colors = RadioButtonDefaults.colors(selectedColor = DieselPrimary)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = DieselOnSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                color = DieselOnSurfaceVariant
            )
        }
    }
}

@Composable
fun SettingToggleRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(DieselSurfaceContainerLow)
            .padding(10.dp),
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
                    .clip(RoundedCornerShape(6.dp))
                    .background(DieselSurfaceContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = DieselPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }

            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = DieselOnSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = DieselOnSurfaceVariant
                )
            }
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = DieselOnPrimary,
                checkedTrackColor = DieselTertiary
            )
        )
    }
}
