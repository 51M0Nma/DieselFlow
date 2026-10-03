package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.DailyLogEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyLogDao {
    @Query("SELECT * FROM daily_logs ORDER BY dayNumber ASC")
    fun getAllLogs(): Flow<List<DailyLogEntry>>

    @Query("SELECT * FROM daily_logs WHERE dateKey = :dateKey LIMIT 1")
    fun getLogByDate(dateKey: String): Flow<DailyLogEntry?>

    @Query("SELECT * FROM daily_logs WHERE dateKey = :dateKey LIMIT 1")
    suspend fun getLogByDateSync(dateKey: String): DailyLogEntry?

    @Query("SELECT * FROM daily_logs WHERE dayNumber = :dayNumber LIMIT 1")
    suspend fun getLogByDay(dayNumber: Int): DailyLogEntry?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(entry: DailyLogEntry)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entries: List<DailyLogEntry>)

    @Update
    suspend fun updateLog(entry: DailyLogEntry)

    @Query("UPDATE daily_logs SET colL_rate = :newRate")
    suspend fun updateRateForAll(newRate: Double)

    @Query("UPDATE daily_logs SET colL_rate = :newRate WHERE colL_rate = 0.0 OR colL_rate IS NULL")
    suspend fun updateRateForBlankOnly(newRate: Double)

    @Query("UPDATE daily_logs SET colL_rate = :newRate WHERE dayNumber >= :fromDay")
    suspend fun updateRateFromDateForward(newRate: Double, fromDay: Int)

    @Query("SELECT COUNT(*) FROM daily_logs")
    suspend fun count(): Int

    @Query("DELETE FROM daily_logs")
    suspend fun deleteAllLogs()
}
