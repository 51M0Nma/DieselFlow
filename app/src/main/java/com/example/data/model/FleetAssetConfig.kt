package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "fleet_assets")
data class FleetAssetConfig(
    @PrimaryKey val colKey: String, // "B", "C", "D", "E", "F", "G", "H", "U8", "U9", etc.
    val unitNumber: Int,            // 1, 2, 3, 4, ...
    val unitName: String = "Unit $unitNumber", // Custom Unit Name
    val licensePlate: String,       // e.g. "GA 2352"
    val vehicleType: String,        // e.g. "40T Hauler"
    val meterInfo: String = "Main Dispenser", // e.g. "Meter: 148,290 km • Main Dispenser"
    val isIdle: Boolean = false,
    val idleReason: String = "",
    val defaultDriver: String = "",
    val capacityLiters: Double = 500.0
)
