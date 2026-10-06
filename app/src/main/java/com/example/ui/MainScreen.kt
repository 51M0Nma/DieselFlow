package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.UserEntity
import com.example.data.repository.DieselFlowRepository
import com.example.ui.components.AccessDeniedScreen
import com.example.ui.components.BottomNavigationBar
import com.example.ui.components.NavigationTab
import com.example.ui.components.TopNavigationHeader
import com.example.ui.components.UnitManagementBottomSheet
import com.example.ui.components.UserConsoleBottomSheet
import com.example.ui.screens.auth.LoginScreen
import com.example.ui.screens.dailylog.DailyLogScreen
import com.example.ui.screens.fleetsummary.FleetSummaryScreen
import com.example.ui.screens.monthlygrid.MonthlyGridScreen
import com.example.ui.screens.settings.SettingsRatesScreen

@Composable
fun MainScreen(
    repository: DieselFlowRepository,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(Unit) {
        repository.checkAndSeed()
    }

    var currentUser by remember { mutableStateOf<UserEntity?>(null) }

    // If user is not authenticated, lock to Login Screen. No one can bypass login.
    if (currentUser == null) {
        LoginScreen(
            repository = repository,
            onLoginSuccess = { user ->
                currentUser = user
            },
            modifier = modifier
        )
        return
    }

    val allLogs by repository.allLogs.collectAsStateWithLifecycle(initialValue = emptyList())
    val allAssets by repository.allAssets.collectAsStateWithLifecycle(initialValue = emptyList())
    val allUsers by repository.allUsers.collectAsStateWithLifecycle(initialValue = emptyList())

    // Reactively refresh current user from database whenever Admin modifies permissions in real-time
    val user = allUsers.find { it.username.equals(currentUser?.username, ignoreCase = true) } ?: currentUser!!

    // If account was disabled by Admin, auto-logout immediately
    if (!user.isActive && !user.isAdmin) {
        currentUser = null
        return
    }

    // Function to resolve first allowed tab for restricted users
    fun getFirstAllowedTab(): NavigationTab {
        return when {
            user.canViewDailyLog || user.isAdmin -> NavigationTab.DAILY_LOG
            user.canViewMonthlyGrid || user.isAdmin -> NavigationTab.MONTHLY_GRID
            user.canViewFleetSummary || user.isAdmin -> NavigationTab.FLEET_SUMMARY
            user.canEditSettings || user.isAdmin -> NavigationTab.SETTINGS_RATES
            else -> NavigationTab.DAILY_LOG
        }
    }

    var currentTab by remember { mutableStateOf(getFirstAllowedTab()) }
    var selectedDay by remember { mutableIntStateOf(24) }

    var showUserConsole by remember { mutableStateOf(false) }
    var showUnitManagement by remember { mutableStateOf(false) }

    val defaultAllowedTab = getFirstAllowedTab()

    // BackHandler: return to first allowed tab
    BackHandler(enabled = currentTab != defaultAllowedTab) {
        currentTab = defaultAllowedTab
    }

    val headerSubtitle = when (currentTab) {
        NavigationTab.DAILY_LOG -> "Daily Log (INR)"
        NavigationTab.MONTHLY_GRID -> "Monthly Grid"
        NavigationTab.FLEET_SUMMARY -> "Fleet Summary"
        NavigationTab.SETTINGS_RATES -> "Settings / Rates"
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopNavigationHeader(
                subtitle = headerSubtitle,
                selectedDay = selectedDay,
                currentUser = user,
                onPreviousDay = {
                    if (selectedDay > 1) selectedDay-- else selectedDay = 31
                },
                onNextDay = {
                    if (selectedDay < 31) selectedDay++ else selectedDay = 1
                },
                onDateClick = {
                    selectedDay = 24 // Reset to Today
                },
                onOpenUserConsole = {
                    if (user.canManageUsers || user.isAdmin) {
                        showUserConsole = true
                    }
                },
                onOpenUnitManagement = {
                    if (user.canManageUnits || user.isAdmin) {
                        showUnitManagement = true
                    }
                },
                onLogout = {
                    currentUser = null
                }
            )
        },
        bottomBar = {
            BottomNavigationBar(
                currentTab = currentTab,
                currentUser = user,
                onTabSelected = { currentTab = it }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = currentTab,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "ScreenTransition"
            ) { targetTab ->
                when (targetTab) {
                    NavigationTab.DAILY_LOG -> {
                        if (user.canViewDailyLog || user.isAdmin) {
                            DailyLogScreen(
                                currentDay = selectedDay,
                                onSelectDay = { selectedDay = it },
                                repository = repository,
                                allLogs = allLogs,
                                assets = allAssets,
                                currentUser = user
                            )
                        } else {
                            AccessDeniedScreen(
                                screenTitle = "Daily Fuel Log Terminal",
                                requiredPermissionName = "Can View Daily Log",
                                currentUser = user,
                                onNavigateBack = { currentTab = getFirstAllowedTab() }
                            )
                        }
                    }

                    NavigationTab.MONTHLY_GRID -> {
                        if (user.canViewMonthlyGrid || user.isAdmin) {
                            MonthlyGridScreen(
                                currentDay = selectedDay,
                                onSelectDay = { selectedDay = it },
                                allLogs = allLogs,
                                assets = allAssets,
                                repository = repository,
                                currentUser = user,
                                onNavigateToDailyLog = { currentTab = NavigationTab.DAILY_LOG }
                            )
                        } else {
                            AccessDeniedScreen(
                                screenTitle = "Monthly Grid Spreadsheet",
                                requiredPermissionName = "Can View Monthly Grid",
                                currentUser = user,
                                onNavigateBack = { currentTab = getFirstAllowedTab() }
                            )
                        }
                    }

                    NavigationTab.FLEET_SUMMARY -> {
                        if (user.canViewFleetSummary || user.isAdmin) {
                            FleetSummaryScreen(
                                allLogs = allLogs,
                                assets = allAssets
                            )
                        } else {
                            AccessDeniedScreen(
                                screenTitle = "Fleet Summary Analytics",
                                requiredPermissionName = "Can View Fleet Summary",
                                currentUser = user,
                                onNavigateBack = { currentTab = getFirstAllowedTab() }
                            )
                        }
                    }

                    NavigationTab.SETTINGS_RATES -> {
                        if (user.canEditSettings || user.isAdmin) {
                            SettingsRatesScreen(
                                assets = allAssets,
                                repository = repository,
                                currentUser = user,
                                onOpenUserConsole = { showUserConsole = true },
                                onOpenUnitManagement = { showUnitManagement = true }
                            )
                        } else {
                            AccessDeniedScreen(
                                screenTitle = "Settings & Master Rates",
                                requiredPermissionName = "Can Edit Settings & Fuel Rates",
                                currentUser = user,
                                onNavigateBack = { currentTab = getFirstAllowedTab() }
                            )
                        }
                    }
                }
            }
        }
    }

    // Modal Overlays strictly guarded by permissions
    if (showUserConsole && (user.canManageUsers || user.isAdmin)) {
        UserConsoleBottomSheet(
            users = allUsers,
            currentUser = user,
            repository = repository,
            onDismiss = { showUserConsole = false }
        )
    }

    if (showUnitManagement && (user.canManageUnits || user.isAdmin)) {
        UnitManagementBottomSheet(
            assets = allAssets,
            repository = repository,
            onDismiss = { showUnitManagement = false }
        )
    }
}
