package com.example.data.repository

import android.util.Log
import com.example.data.local.DailyLogDao
import com.example.data.local.DatabaseSeeder
import com.example.data.local.FleetAssetDao
import com.example.data.local.UserDao
import com.example.data.model.DailyLogEntry
import com.example.data.model.FleetAssetConfig
import com.example.data.model.UserEntity
import com.example.data.remote.GoogleSheetsService
import com.example.data.remote.SupabaseService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DieselFlowRepository(
    private val dailyLogDao: DailyLogDao,
    private val fleetAssetDao: FleetAssetDao,
    private val userDao: UserDao
) {
    companion object {
        private const val TAG = "DieselFlowRepository"
    }

    val allLogs: Flow<List<DailyLogEntry>> = dailyLogDao.getAllLogs()
    val allAssets: Flow<List<FleetAssetConfig>> = fleetAssetDao.getAllAssets()
    val allUsers: Flow<List<UserEntity>> = userDao.getAllUsers()

    suspend fun checkAndSeed() = withContext(Dispatchers.IO) {
        // Ensure default local Admin exists
        val defaultAdmin = UserEntity(
            username = "Simon-Mahajan",
            password = "51M0N@P455w0rd",
            fullName = "Simon Mahajan",
            role = "ADMIN",
            canEditDailyLog = true,
            canViewDailyLog = true,
            canEditMonthlyGrid = true,
            canViewMonthlyGrid = true,
            canViewFleetSummary = true,
            canEditSettings = true,
            canManageUsers = true,
            canManageUnits = true,
            isActive = true
        )

        val localAdmin = userDao.getUserByUsername("Simon-Mahajan")
        if (localAdmin == null) {
            userDao.insertUser(defaultAdmin)
        }

        if (fleetAssetDao.count() == 0) {
            fleetAssetDao.insertAll(DatabaseSeeder.getDefaultFleetAssets())
        }
        if (dailyLogDao.count() == 0) {
            dailyLogDao.insertAll(DatabaseSeeder.getCleanOctoberLogs())
        }

        // Two-way synchronization with Supabase Cloud
        try {
            syncWithSupabase()
        } catch (e: Exception) {
            Log.e(TAG, "Supabase initial sync exception", e)
        }
    }

    // Full 2-way cloud sync with Supabase
    suspend fun syncWithSupabase(): Boolean = withContext(Dispatchers.IO) {
        try {
            // 1. Sync Users
            val remoteUsers = SupabaseService.fetchUsers()
            if (remoteUsers.isNotEmpty()) {
                userDao.insertAll(remoteUsers)
            } else {
                // Cloud table is empty, seed default admin to Supabase
                val localAdmin = userDao.getUserByUsername("Simon-Mahajan")
                if (localAdmin != null) {
                    SupabaseService.pushUser(localAdmin)
                }
            }

            // 2. Sync Fleet Assets
            val remoteAssets = SupabaseService.fetchFleetAssets()
            if (remoteAssets.isNotEmpty()) {
                fleetAssetDao.insertAll(remoteAssets)
            } else {
                val localAssets = fleetAssetDao.getAllAssetsSync()
                localAssets.forEach { SupabaseService.pushFleetAsset(it) }
            }

            // 3. Sync Daily Logs
            val remoteLogs = SupabaseService.fetchDailyLogs()
            if (remoteLogs.isNotEmpty()) {
                dailyLogDao.insertAll(remoteLogs)
            } else {
                val localLogs = DatabaseSeeder.getCleanOctoberLogs()
                SupabaseService.pushAllDailyLogs(localLogs)
            }

            true
        } catch (e: Exception) {
            Log.e(TAG, "Sync with Supabase failed", e)
            false
        }
    }

    // --- Authentication & User Management ---
    suspend fun authenticate(username: String, password: String): UserEntity? = withContext(Dispatchers.IO) {
        val trimmedUser = username.trim()
        val localUser = userDao.getUserByUsername(trimmedUser)

        if (localUser != null && localUser.password == password && localUser.isActive) {
            // Attempt cloud sync in background
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val remoteUsers = SupabaseService.fetchUsers()
                    if (remoteUsers.isNotEmpty()) {
                        userDao.insertAll(remoteUsers)
                    }
                } catch (_: Exception) {}
            }
            return@withContext localUser
        }

        // Try authenticating directly against Supabase if local user wasn't cached yet
        try {
            val remoteUsers = SupabaseService.fetchUsers()
            if (remoteUsers.isNotEmpty()) {
                userDao.insertAll(remoteUsers)
                val refreshedUser = userDao.getUserByUsername(trimmedUser)
                if (refreshedUser != null && refreshedUser.password == password && refreshedUser.isActive) {
                    return@withContext refreshedUser
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Remote auth failed", e)
        }

        null
    }

    suspend fun saveUser(user: UserEntity) = withContext(Dispatchers.IO) {
        if (user.id == 0) {
            userDao.insertUser(user)
        } else {
            userDao.updateUser(user)
        }

        // Push to Supabase Cloud
        CoroutineScope(Dispatchers.IO).launch {
            SupabaseService.pushUser(user)
        }
    }

    suspend fun deleteUser(user: UserEntity) = withContext(Dispatchers.IO) {
        userDao.deleteUserById(user.id)
        CoroutineScope(Dispatchers.IO).launch {
            SupabaseService.deleteUser(user.username)
        }
    }

    suspend fun deleteUser(userId: Int) = withContext(Dispatchers.IO) {
        val user = userDao.getUserById(userId)
        userDao.deleteUserById(userId)
        if (user != null) {
            CoroutineScope(Dispatchers.IO).launch {
                SupabaseService.deleteUser(user.username)
            }
        }
    }

    // --- Fleet Unit CRUD ---
    suspend fun saveFleetUnit(unit: FleetAssetConfig) = withContext(Dispatchers.IO) {
        fleetAssetDao.insertAsset(unit)
        CoroutineScope(Dispatchers.IO).launch {
            SupabaseService.pushFleetAsset(unit)
        }
    }

    suspend fun deleteFleetUnit(colKey: String) = withContext(Dispatchers.IO) {
        fleetAssetDao.deleteAssetByColKey(colKey)
        CoroutineScope(Dispatchers.IO).launch {
            SupabaseService.deleteFleetAsset(colKey)
        }
    }

    suspend fun getNextUnitNumber(): Int = withContext(Dispatchers.IO) {
        (fleetAssetDao.getMaxUnitNumber() ?: 7) + 1
    }

    suspend fun updateAssetLicensePlate(colKey: String, plate: String) = withContext(Dispatchers.IO) {
        fleetAssetDao.updateLicensePlate(colKey, plate)
        val updated = fleetAssetDao.getAssetByColKey(colKey)
        if (updated != null) {
            CoroutineScope(Dispatchers.IO).launch {
                SupabaseService.pushFleetAsset(updated)
            }
        }
    }

    // --- Daily Logs ---
    suspend fun clearAndResetToBlank() = withContext(Dispatchers.IO) {
        dailyLogDao.deleteAllLogs()
        val cleanLogs = DatabaseSeeder.getCleanOctoberLogs()
        dailyLogDao.insertAll(cleanLogs)
        CoroutineScope(Dispatchers.IO).launch {
            SupabaseService.pushAllDailyLogs(cleanLogs)
        }
    }

    fun getLogByDate(dateKey: String): Flow<DailyLogEntry?> {
        return dailyLogDao.getLogByDate(dateKey)
    }

    suspend fun getLogByDateSync(dateKey: String): DailyLogEntry? = withContext(Dispatchers.IO) {
        dailyLogDao.getLogByDateSync(dateKey)
    }

    suspend fun getLogByDay(day: Int): DailyLogEntry? = withContext(Dispatchers.IO) {
        dailyLogDao.getLogByDay(day)
    }

    suspend fun saveOrUpdateLog(entry: DailyLogEntry) = withContext(Dispatchers.IO) {
        val updated = entry.copy(isSynced = true)
        dailyLogDao.insertOrUpdate(updated)
        CoroutineScope(Dispatchers.IO).launch {
            SupabaseService.pushDailyLog(updated)
        }
    }

    suspend fun applyRate(rate: Double, scope: String, currentDay: Int = 24) = withContext(Dispatchers.IO) {
        when (scope) {
            "all" -> dailyLogDao.updateRateForAll(rate)
            "blank" -> dailyLogDao.updateRateForBlankOnly(rate)
            "forward" -> dailyLogDao.updateRateFromDateForward(rate, currentDay)
            else -> dailyLogDao.updateRateForAll(rate)
        }

        // Push all updated logs to Supabase
        CoroutineScope(Dispatchers.IO).launch {
            val all = DatabaseSeeder.getCleanOctoberLogs() // or fetch from DAO
            SupabaseService.pushAllDailyLogs(all)
        }
    }
}
