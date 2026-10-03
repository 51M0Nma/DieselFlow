package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.json.JSONObject

@Entity(tableName = "daily_logs")
data class DailyLogEntry(
    @PrimaryKey val dateKey: String, // e.g. "2026-10-24"
    val dayNumber: Int,              // 1..31
    val monthYear: String = "October 2026",
    val dayOfWeek: String = "THU",
    val colB_unit1: Double = 0.0,    // GA 2352
    val colC_unit2: Double = 0.0,    // GA 2332
    val colD_unit3: Double = 0.0,    // GA 4109
    val colE_unit4: Double = 0.0,    // GA 8812
    val colF_unit5: Double = 0.0,    // GA 9011
    val colG_unit6: Double = 0.0,    // GA 3345
    val colH_unit7: Double = 0.0,    // GA 5520
    val customUnitReadingsJson: String = "{}", // For any dynamically added units (e.g. U8, U9, etc.)
    val colI_plant: Double = 0.0,    // Facility & Plant Generator Liters
    val plantRemarks: String = "Main Yard Backup Power • Runtime: 14 hrs",
    val issuedToName: String = "",   // Issued To Recipient / Destination Name
    val issuedToLiters: Double = 0.0,// Issued To Liters filled
    val issuedToRemarks: String = "",// Issued To specific notes
    val colJ_issueTo: String = "",   // Legacy compatibility
    val colK_remarks: String = "",   // Shift general remarks
    val colL_rate: Double = 92.50,   // ₹/L Fuel Price Matrix
    val isLogged: Boolean = false,   // True if recorded
    val isSynced: Boolean = false,   // Synced to Google Sheet
    val slipAttached: Boolean = true,
    val loggedBy: String = "Depot Foreman M. Ramos"
) {
    val targetRow: Int
        get() = dayNumber + 2

    fun getUnitReading(colKey: String): Double {
        return when (colKey.uppercase()) {
            "B" -> colB_unit1
            "C" -> colC_unit2
            "D" -> colD_unit3
            "E" -> colE_unit4
            "F" -> colF_unit5
            "G" -> colG_unit6
            "H" -> colH_unit7
            else -> {
                try {
                    val json = JSONObject(customUnitReadingsJson)
                    json.optDouble(colKey, 0.0)
                } catch (e: Exception) {
                    0.0
                }
            }
        }
    }

    fun setUnitReading(colKey: String, value: Double): DailyLogEntry {
        return when (colKey.uppercase()) {
            "B" -> copy(colB_unit1 = value)
            "C" -> copy(colC_unit2 = value)
            "D" -> copy(colD_unit3 = value)
            "E" -> copy(colE_unit4 = value)
            "F" -> copy(colF_unit5 = value)
            "G" -> copy(colG_unit6 = value)
            "H" -> copy(colH_unit7 = value)
            else -> {
                val json = try {
                    JSONObject(customUnitReadingsJson)
                } catch (e: Exception) {
                    JSONObject()
                }
                json.put(colKey, value)
                copy(customUnitReadingsJson = json.toString())
            }
        }
    }

    val customUnitsSum: Double
        get() {
            return try {
                val json = JSONObject(customUnitReadingsJson)
                var sum = 0.0
                val keys = json.keys()
                while (keys.hasNext()) {
                    val k = keys.next()
                    sum += json.optDouble(k, 0.0)
                }
                sum
            } catch (e: Exception) {
                0.0
            }
        }

    val fleetLiters: Double
        get() = colB_unit1 + colC_unit2 + colD_unit3 + colE_unit4 + colF_unit5 + colG_unit6 + colH_unit7 + customUnitsSum

    val totalLiters: Double
        get() = fleetLiters + colI_plant + issuedToLiters

    val calculatedCost: Double
        get() = totalLiters * colL_rate

    val entriesCount: Int
        get() {
            var count = 0
            if (colB_unit1 > 0) count++
            if (colC_unit2 > 0) count++
            if (colD_unit3 > 0) count++
            if (colE_unit4 > 0) count++
            if (colF_unit5 > 0) count++
            if (colG_unit6 > 0) count++
            if (colH_unit7 > 0) count++
            if (colI_plant > 0) count++
            if (issuedToLiters > 0) count++
            try {
                val json = JSONObject(customUnitReadingsJson)
                val keys = json.keys()
                while (keys.hasNext()) {
                    val k = keys.next()
                    if (json.optDouble(k, 0.0) > 0) count++
                }
            } catch (_: Exception) {}
            return count
        }
}
