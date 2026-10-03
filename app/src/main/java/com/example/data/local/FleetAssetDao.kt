package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.FleetAssetConfig
import kotlinx.coroutines.flow.Flow

@Dao
interface FleetAssetDao {
    @Query("SELECT * FROM fleet_assets ORDER BY unitNumber ASC")
    fun getAllAssets(): Flow<List<FleetAssetConfig>>

    @Query("SELECT * FROM fleet_assets ORDER BY unitNumber ASC")
    suspend fun getAllAssetsSync(): List<FleetAssetConfig>

    @Query("SELECT * FROM fleet_assets WHERE colKey = :colKey LIMIT 1")
    suspend fun getAssetByColKey(colKey: String): FleetAssetConfig?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAsset(asset: FleetAssetConfig)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(assets: List<FleetAssetConfig>)

    @Update
    suspend fun updateAsset(asset: FleetAssetConfig)

    @Delete
    suspend fun deleteAsset(asset: FleetAssetConfig)

    @Query("DELETE FROM fleet_assets WHERE colKey = :colKey")
    suspend fun deleteAssetByColKey(colKey: String)

    @Query("UPDATE fleet_assets SET licensePlate = :licensePlate WHERE colKey = :colKey")
    suspend fun updateLicensePlate(colKey: String, licensePlate: String)

    @Query("SELECT COUNT(*) FROM fleet_assets")
    suspend fun count(): Int

    @Query("SELECT MAX(unitNumber) FROM fleet_assets")
    suspend fun getMaxUnitNumber(): Int?
}
