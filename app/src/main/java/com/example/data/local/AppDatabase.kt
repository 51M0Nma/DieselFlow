package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.DailyLogEntry
import com.example.data.model.FleetAssetConfig
import com.example.data.model.UserEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [DailyLogEntry::class, FleetAssetConfig::class, UserEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dailyLogDao(): DailyLogDao
    abstract fun fleetAssetDao(): FleetAssetDao
    abstract fun userDao(): UserDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "dieselflow_database.db"
                )
                .fallbackToDestructiveMigration()
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        // Seed database in coroutine
                        CoroutineScope(Dispatchers.IO).launch {
                            val database = getInstance(context)
                            database.userDao().insertUser(
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
                            database.fleetAssetDao().insertAll(DatabaseSeeder.getDefaultFleetAssets())
                            database.dailyLogDao().insertAll(DatabaseSeeder.getCleanOctoberLogs())
                        }
                    }
                }).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
