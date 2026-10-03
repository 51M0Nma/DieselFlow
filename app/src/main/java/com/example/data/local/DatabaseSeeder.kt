package com.example.data.local

import com.example.data.model.DailyLogEntry
import com.example.data.model.FleetAssetConfig

object DatabaseSeeder {

    fun getDefaultFleetAssets(): List<FleetAssetConfig> {
        return listOf(
            FleetAssetConfig(
                colKey = "B",
                unitNumber = 1,
                licensePlate = "GA 2352",
                vehicleType = "40T Hauler",
                meterInfo = "Main Dispenser",
                isIdle = false
            ),
            FleetAssetConfig(
                colKey = "C",
                unitNumber = 2,
                licensePlate = "GA 2332",
                vehicleType = "30T Rigid",
                meterInfo = "Main Dispenser",
                isIdle = false
            ),
            FleetAssetConfig(
                colKey = "D",
                unitNumber = 3,
                licensePlate = "GA 4109",
                vehicleType = "Flatbed Semi",
                meterInfo = "Bowser Bay 1",
                isIdle = false
            ),
            FleetAssetConfig(
                colKey = "E",
                unitNumber = 4,
                licensePlate = "GA 8812",
                vehicleType = "Tipper Dump",
                meterInfo = "Site Bowser",
                isIdle = false
            ),
            FleetAssetConfig(
                colKey = "F",
                unitNumber = 5,
                licensePlate = "GA 9011",
                vehicleType = "Heavy Crane",
                meterInfo = "Maintenance / Yard Standby",
                isIdle = true,
                idleReason = "Yard standby"
            ),
            FleetAssetConfig(
                colKey = "G",
                unitNumber = 6,
                licensePlate = "GA 3345",
                vehicleType = "Tanker 18kL",
                meterInfo = "Main Dispenser",
                isIdle = false
            ),
            FleetAssetConfig(
                colKey = "H",
                unitNumber = 7,
                licensePlate = "GA 5520",
                vehicleType = "Depot Loader",
                meterInfo = "Main Dispenser",
                isIdle = false
            )
        )
    }

    fun getCleanOctoberLogs(): List<DailyLogEntry> {
        val daysOfWeek = listOf("THU", "FRI", "SAT", "SUN", "MON", "TUE", "WED")
        val list = mutableListOf<DailyLogEntry>()

        for (day in 1..31) {
            val dateKey = "2026-10-${day.toString().padStart(2, '0')}"
            val dow = daysOfWeek[(day - 1) % 7]

            list.add(
                DailyLogEntry(
                    dateKey = dateKey,
                    dayNumber = day,
                    monthYear = "October 2026",
                    dayOfWeek = dow,
                    colB_unit1 = 0.0,
                    colC_unit2 = 0.0,
                    colD_unit3 = 0.0,
                    colE_unit4 = 0.0,
                    colF_unit5 = 0.0,
                    colG_unit6 = 0.0,
                    colH_unit7 = 0.0,
                    colI_plant = 0.0,
                    colJ_issueTo = "Awaiting shift slip",
                    colK_remarks = "",
                    colL_rate = 92.50,
                    isLogged = false,
                    isSynced = false,
                    slipAttached = false,
                    loggedBy = ""
                )
            )
        }
        return list
    }
}
