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

    val user = currentUser!!
    val allLogs by repository.allLogs.collectAsStateWithLifecycle(initialValue = emptyList())
    val allAssets by repository.allAssets.collectAsStateWithLifecycle(initialValue = emptyList())
    val allUsers by repository.allUsers.collectAsStateWithLifecycle(initialValue = emptyList())

    var currentTab by remember { mutableStateOf(NavigationTab.DAILY_LOG) }
    var selectedDay by remember { mutableIntStateOf(24) }

    var showUserConsole by remember { mutableStateOf(false) }
    var showUnitManagement by remember { mutableStateOf(false) }

    // BackHandler: if on modal/console or not on Daily Log, return to Daily Log
    BackHandler(enabled = currentTab != NavigationTab.DAILY_LOG) {
        currentTab = NavigationTab.DAILY_LOG
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
                    showUserConsole = true
                },
                onOpenUnitManagement = {
                    showUnitManagement = true
                },
                onLogout = {
                    currentUser = null
                }
            )
        },
        bottomBar = {
            BottomNavigationBar(
                currentTab = currentTab,
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
                        DailyLogScreen(
                            currentDay = selectedDay,
                            onSelectDay = { selectedDay = it },
                            repository = repository,
                            allLogs = allLogs,
                            assets = allAssets,
                            currentUser = user
                        )
                    }

                    NavigationTab.MONTHLY_GRID -> {
                        MonthlyGridScreen(
                            currentDay = selectedDay,
                            onSelectDay = { selectedDay = it },
                            allLogs = allLogs,
                            assets = allAssets,
                            repository = repository,
                            currentUser = user,
                            onNavigateToDailyLog = { currentTab = NavigationTab.DAILY_LOG }
                        )
                    }

                    NavigationTab.FLEET_SUMMARY -> {
                        FleetSummaryScreen(
                            allLogs = allLogs,
                            assets = allAssets
                        )
                    }

                    NavigationTab.SETTINGS_RATES -> {
                        SettingsRatesScreen(
                            assets = allAssets,
                            repository = repository,
                            currentUser = user,
                            onOpenUserConsole = { showUserConsole = true },
                            onOpenUnitManagement = { showUnitManagement = true }
                        )
                    }
                }
            }
        }
    }

    // Modal Overlays
    if (showUserConsole) {
        UserConsoleBottomSheet(
            users = allUsers,
            currentUser = user,
            repository = repository,
            onDismiss = { showUserConsole = false }
        )
    }

    if (showUnitManagement) {
        UnitManagementBottomSheet(
            assets = allAssets,
            repository = repository,
            onDismiss = { showUnitManagement = false }
        )
    }
}
