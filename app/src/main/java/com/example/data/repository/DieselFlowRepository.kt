package com.example.data.repository

import com.example.data.local.DailyLogDao
import com.example.data.local.DatabaseSeeder
import com.example.data.local.FleetAssetDao
import com.example.data.local.UserDao
import com.example.data.model.DailyLogEntry
import com.example.data.model.FleetAssetConfig
import com.example.data.model.UserEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class DieselFlowRepository(
    private val dailyLogDao: DailyLogDao,
    private val fleetAssetDao: FleetAssetDao,
    private val userDao: UserDao
) {

    val allLogs: Flow<List<DailyLogEntry>> = dailyLogDao.getAllLogs()
    val allAssets: Flow<List<FleetAssetConfig>> = fleetAssetDao.getAllAssets()
    val allUsers: Flow<List<UserEntity>> = userDao.getAllUsers()

    suspend fun checkAndSeed() = withContext(Dispatchers.IO) {
        // Ensure default Admin exists
        val admin = userDao.getUserByUsername("Simon-Mahajan")
        if (admin == null) {
            userDao.insertUser(
                UserEntity(
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
            )
        }

        if (fleetAssetDao.count() == 0) {
            fleetAssetDao.insertAll(DatabaseSeeder.getDefaultFleetAssets())
        }
        if (dailyLogDao.count() == 0) {
            dailyLogDao.insertAll(DatabaseSeeder.getCleanOctoberLogs())
        }
    }

    // --- Authentication & User Management ---
    suspend fun authenticate(username: String, password: String): UserEntity? = withContext(Dispatchers.IO) {
        val user = userDao.getUserByUsername(username.trim())
        if (user != null && user.password == password && user.isActive) {
            user
        } else {
            null
        }
    }

    suspend fun saveUser(user: UserEntity) = withContext(Dispatchers.IO) {
        if (user.id == 0) {
            userDao.insertUser(user)
        } else {
            userDao.updateUser(user)
        }
    }

    suspend fun deleteUser(userId: Int) = withContext(Dispatchers.IO) {
        userDao.deleteUserById(userId)
    }

    // --- Fleet Unit CRUD ---
    suspend fun saveFleetUnit(unit: FleetAssetConfig) = withContext(Dispatchers.IO) {
        fleetAssetDao.insertAsset(unit)
    }

    suspend fun deleteFleetUnit(colKey: String) = withContext(Dispatchers.IO) {
        fleetAssetDao.deleteAssetByColKey(colKey)
    }

    suspend fun getNextUnitNumber(): Int = withContext(Dispatchers.IO) {
        (fleetAssetDao.getMaxUnitNumber() ?: 7) + 1
    }

    suspend fun updateAssetLicensePlate(colKey: String, plate: String) = withContext(Dispatchers.IO) {
        fleetAssetDao.updateLicensePlate(colKey, plate)
    }

    // --- Daily Logs ---
    suspend fun clearAndResetToBlank() = withContext(Dispatchers.IO) {
        dailyLogDao.deleteAllLogs()
        dailyLogDao.insertAll(DatabaseSeeder.getCleanOctoberLogs())
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
        dailyLogDao.insertOrUpdate(entry)
    }

    suspend fun applyRate(rate: Double, scope: String, currentDay: Int = 24) = withContext(Dispatchers.IO) {
        when (scope) {
            "all" -> dailyLogDao.updateRateForAll(rate)
            "blank" -> dailyLogDao.updateRateForBlankOnly(rate)
            "forward" -> dailyLogDao.updateRateFromDateForward(rate, currentDay)
            else -> dailyLogDao.updateRateForAll(rate)
        }
    }
}
