package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.CurrencyRupee
import androidx.compose.material.icons.outlined.GridOn
import androidx.compose.material.icons.outlined.LocalGasStation
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserEntity
import com.example.ui.theme.DieselError
import com.example.ui.theme.DieselOnSurfaceVariant
import com.example.ui.theme.DieselPrimary
import com.example.ui.theme.DieselSecondary

enum class NavigationTab(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
) {
    DAILY_LOG("Daily Log", Icons.Filled.LocalGasStation, Icons.Outlined.LocalGasStation, "tab_daily_log"),
    MONTHLY_GRID("Monthly Grid", Icons.Filled.GridOn, Icons.Outlined.GridOn, "tab_monthly_grid"),
    FLEET_SUMMARY("Fleet Summary", Icons.Filled.BarChart, Icons.Outlined.BarChart, "tab_fleet_summary"),
    SETTINGS_RATES("Settings / Rates", Icons.Filled.CurrencyRupee, Icons.Outlined.CurrencyRupee, "tab_settings_rates")
}

@Composable
fun BottomNavigationBar(
    currentTab: NavigationTab,
    currentUser: UserEntity,
    onTabSelected: (NavigationTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("bottom_navigation_bar"),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .height(60.dp)
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavigationTab.entries.forEach { tab ->
                val isSelected = currentTab == tab
                val interactionSource = remember { MutableInteractionSource() }

                val isAllowed = when (tab) {
                    NavigationTab.DAILY_LOG -> currentUser.canViewDailyLog || currentUser.isAdmin
                    NavigationTab.MONTHLY_GRID -> currentUser.canViewMonthlyGrid || currentUser.isAdmin
                    NavigationTab.FLEET_SUMMARY -> currentUser.canViewFleetSummary || currentUser.isAdmin
                    NavigationTab.SETTINGS_RATES -> currentUser.canEditSettings || currentUser.isAdmin
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null
                        ) { onTabSelected(tab) }
                        .padding(vertical = 6.dp)
                        .testTag(tab.testTag)
                        .alpha(if (isAllowed) 1f else 0.55f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(contentAlignment = Alignment.TopEnd) {
                        Icon(
                            imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                            contentDescription = tab.title,
                            tint = when {
                                isSelected -> DieselPrimary
                                !isAllowed -> DieselSecondary
                                else -> DieselOnSurfaceVariant
                            },
                            modifier = Modifier.size(22.dp)
                        )

                        if (!isAllowed) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(DieselError),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "Locked",
                                    tint = MaterialTheme.colorScheme.onError,
                                    modifier = Modifier.size(7.dp)
                                )
                            }
                        }
                    }

                    Text(
                        text = tab.title,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        ),
                        color = when {
                            isSelected -> DieselPrimary
                            !isAllowed -> DieselSecondary
                            else -> DieselOnSurfaceVariant
                        }
                    )
                }
            }
        }
    }
}
