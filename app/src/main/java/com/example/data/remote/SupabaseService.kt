package com.example.data.remote

import android.util.Log
import com.example.BuildConfig
import com.example.data.model.DailyLogEntry
import com.example.data.model.FleetAssetConfig
import com.example.data.model.UserEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object SupabaseService {
    private const val TAG = "SupabaseService"
    private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()

    val baseUrl: String
        get() {
            val url = try {
                BuildConfig.SUPABASE_URL
            } catch (e: Exception) {
                ""
            }
            return if (url.isNotBlank() && url != "null") {
                url.trimEnd('/')
            } else {
                "https://riehesxahscrbcdxmhlr.supabase.co"
            }
        }

    val anonKey: String
        get() {
            val key = try {
                BuildConfig.SUPABASE_ANON_KEY
            } catch (e: Exception) {
                ""
            }
            return if (key.isNotBlank() && key != "null") {
                key
            } else {
                "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InJpZWhlc3hhaHNjcmJjZHhtaGxyIiwicm9sZSI6ImFub24iLCJpYXQiOjE3OTEwMTgwMTcsImV4cCI6MjEwNjU5NDAxN30.UuHjaZ3HNxLES7bjIuQkwVoiXkHCIiN958xuWbJOois"
            }
        }

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    private fun newRequestBuilder(endpoint: String): Request.Builder {
        return Request.Builder()
            .url("$baseUrl/rest/v1/$endpoint")
            .addHeader("apikey", anonKey)
            .addHeader("Authorization", "Bearer $anonKey")
            .addHeader("Content-Type", "application/json")
    }

    // Ping / Test connection to Supabase
    suspend fun testConnection(): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val request = newRequestBuilder("app_users?select=count").head().build()
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful || response.code in 200..299) {
                    Result.success(true)
                } else {
                    Result.failure(Exception("HTTP ${response.code}: ${response.message}"))
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Connection test failed", e)
            Result.failure(e)
        }
    }

    // --- Users Table (app_users) ---
    suspend fun fetchUsers(): List<UserEntity> = withContext(Dispatchers.IO) {
        try {
            val request = newRequestBuilder("app_users?select=*").get().build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    Log.w(TAG, "fetchUsers failed code: ${response.code}")
                    return@withContext emptyList()
                }
                val body = response.body?.string() ?: return@withContext emptyList()
                val array = JSONArray(body)
                val list = mutableListOf<UserEntity>()
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    list.add(
                        UserEntity(
                            id = obj.optInt("id", 0),
                            username = obj.optString("username", ""),
                            password = obj.optString("password", ""),
                            fullName = obj.optString("full_name", ""),
                            role = obj.optString("role", "OPERATOR"),
                            canEditDailyLog = obj.optBoolean("can_edit_daily_log", true),
                            canViewDailyLog = obj.optBoolean("can_view_daily_log", true),
                            canEditMonthlyGrid = obj.optBoolean("can_edit_monthly_grid", true),
                            canViewMonthlyGrid = obj.optBoolean("can_view_monthly_grid", true),
                            canViewFleetSummary = obj.optBoolean("can_view_fleet_summary", true),
                            canEditSettings = obj.optBoolean("can_edit_settings", false),
                            canManageUsers = obj.optBoolean("can_manage_users", false),
                            canManageUnits = obj.optBoolean("can_manage_units", false),
                            isActive = obj.optBoolean("is_active", true)
                        )
                    )
                }
                list
            }
        } catch (e: Exception) {
            Log.e(TAG, "fetchUsers exception", e)
            emptyList()
        }
    }

    suspend fun pushUser(user: UserEntity): Boolean = withContext(Dispatchers.IO) {
        try {
            val json = JSONObject().apply {
                put("username", user.username)
                put("password", user.password)
                put("full_name", user.fullName)
                put("role", user.role)
                put("can_edit_daily_log", user.canEditDailyLog)
                put("can_view_daily_log", user.canViewDailyLog)
                put("can_edit_monthly_grid", user.canEditMonthlyGrid)
                put("can_view_monthly_grid", user.canViewMonthlyGrid)
                put("can_view_fleet_summary", user.canViewFleetSummary)
                put("can_edit_settings", user.canEditSettings)
                put("can_manage_users", user.canManageUsers)
                put("can_manage_units", user.canManageUnits)
                put("is_active", user.isActive)
            }

            val request = newRequestBuilder("app_users?on_conflict=username")
                .addHeader("Prefer", "resolution=merge-duplicates")
                .post(json.toString().toRequestBody(JSON_MEDIA_TYPE))
                .build()

            client.newCall(request).execute().use { response ->
                response.isSuccessful || response.code in 200..299
            }
        } catch (e: Exception) {
            Log.e(TAG, "pushUser error", e)
            false
        }
    }

    suspend fun deleteUser(username: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val request = newRequestBuilder("app_users?username=eq.$username")
                .delete()
                .build()

            client.newCall(request).execute().use { response ->
                response.isSuccessful || response.code in 200..299
            }
        } catch (e: Exception) {
            Log.e(TAG, "deleteUser error", e)
            false
        }
    }

    // --- Fleet Assets Table (fleet_assets) ---
    suspend fun fetchFleetAssets(): List<FleetAssetConfig> = withContext(Dispatchers.IO) {
        try {
            val request = newRequestBuilder("fleet_assets?select=*&order=unit_number.asc").get().build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    Log.w(TAG, "fetchFleetAssets failed: ${response.code}")
                    return@withContext emptyList()
                }
                val body = response.body?.string() ?: return@withContext emptyList()
                val array = JSONArray(body)
                val list = mutableListOf<FleetAssetConfig>()
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    list.add(
                        FleetAssetConfig(
                            colKey = obj.optString("col_key", "B"),
                            unitNumber = obj.optInt("unit_number", 1),
                            unitName = obj.optString("unit_name", "Unit 1"),
                            licensePlate = obj.optString("license_plate", ""),
                            vehicleType = obj.optString("vehicle_type", "Fleet Truck"),
                            meterInfo = obj.optString("meter_info", "Main Dispenser"),
                            isIdle = obj.optBoolean("is_idle", false),
                            idleReason = obj.optString("idle_reason", ""),
                            capacityLiters = obj.optDouble("capacity_liters", 500.0)
                        )
                    )
                }
                list
            }
        } catch (e: Exception) {
            Log.e(TAG, "fetchFleetAssets exception", e)
            emptyList()
        }
    }

    suspend fun pushFleetAsset(asset: FleetAssetConfig): Boolean = withContext(Dispatchers.IO) {
        try {
            val json = JSONObject().apply {
                put("col_key", asset.colKey)
                put("unit_number", asset.unitNumber)
                put("unit_name", asset.unitName)
                put("license_plate", asset.licensePlate)
                put("vehicle_type", asset.vehicleType)
                put("meter_info", asset.meterInfo)
                put("is_idle", asset.isIdle)
                put("idle_reason", asset.idleReason)
                put("capacity_liters", asset.capacityLiters)
            }

            val request = newRequestBuilder("fleet_assets?on_conflict=col_key")
                .addHeader("Prefer", "resolution=merge-duplicates")
                .post(json.toString().toRequestBody(JSON_MEDIA_TYPE))
                .build()

            client.newCall(request).execute().use { response ->
                response.isSuccessful || response.code in 200..299
            }
        } catch (e: Exception) {
            Log.e(TAG, "pushFleetAsset error", e)
            false
        }
    }

    suspend fun deleteFleetAsset(colKey: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val request = newRequestBuilder("fleet_assets?col_key=eq.$colKey")
                .delete()
                .build()

            client.newCall(request).execute().use { response ->
                response.isSuccessful || response.code in 200..299
            }
        } catch (e: Exception) {
            Log.e(TAG, "deleteFleetAsset error", e)
            false
        }
    }

    // --- Daily Logs Table (daily_logs) ---
    suspend fun fetchDailyLogs(): List<DailyLogEntry> = withContext(Dispatchers.IO) {
        try {
            val request = newRequestBuilder("daily_logs?select=*&order=day_number.asc").get().build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    Log.w(TAG, "fetchDailyLogs failed: ${response.code}")
                    return@withContext emptyList()
                }
                val body = response.body?.string() ?: return@withContext emptyList()
                val array = JSONArray(body)
                val list = mutableListOf<DailyLogEntry>()
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    list.add(
                        DailyLogEntry(
                            dateKey = obj.optString("date_key", ""),
                            dayNumber = obj.optInt("day_number", 1),
                            monthYear = obj.optString("month_year", "October 2026"),
                            dayOfWeek = obj.optString("day_of_week", "THU"),
                            colB_unit1 = obj.optDouble("col_b_unit1", 0.0),
                            colC_unit2 = obj.optDouble("col_c_unit2", 0.0),
                            colD_unit3 = obj.optDouble("col_d_unit3", 0.0),
                            colE_unit4 = obj.optDouble("col_e_unit4", 0.0),
                            colF_unit5 = obj.optDouble("col_f_unit5", 0.0),
                            colG_unit6 = obj.optDouble("col_g_unit6", 0.0),
                            colH_unit7 = obj.optDouble("col_h_unit7", 0.0),
                            customUnitReadingsJson = obj.opt("custom_unit_readings_json")?.toString() ?: "{}",
                            colI_plant = obj.optDouble("col_i_plant", 0.0),
                            plantRemarks = obj.optString("plant_remarks", ""),
                            issuedToName = obj.optString("issued_to_name", ""),
                            issuedToLiters = obj.optDouble("issued_to_liters", 0.0),
                            issuedToRemarks = obj.optString("issued_to_remarks", ""),
                            colJ_issueTo = obj.optString("col_j_issue_to", ""),
                            colK_remarks = obj.optString("col_k_remarks", ""),
                            colL_rate = obj.optDouble("col_l_rate", 92.50),
                            isLogged = obj.optBoolean("is_logged", false),
                            isSynced = obj.optBoolean("is_synced", true),
                            slipAttached = obj.optBoolean("slip_attached", true),
                            loggedBy = obj.optString("logged_by", "")
                        )
                    )
                }
                list
            }
        } catch (e: Exception) {
            Log.e(TAG, "fetchDailyLogs exception", e)
            emptyList()
        }
    }

    suspend fun pushDailyLog(entry: DailyLogEntry): Boolean = withContext(Dispatchers.IO) {
        try {
            val json = JSONObject().apply {
                put("date_key", entry.dateKey)
                put("day_number", entry.dayNumber)
                put("month_year", entry.monthYear)
                put("day_of_week", entry.dayOfWeek)
                put("col_b_unit1", entry.colB_unit1)
                put("col_c_unit2", entry.colC_unit2)
                put("col_d_unit3", entry.colD_unit3)
                put("col_e_unit4", entry.colE_unit4)
                put("col_f_unit5", entry.colF_unit5)
                put("col_g_unit6", entry.colG_unit6)
                put("col_h_unit7", entry.colH_unit7)
                val customJson = try {
                    JSONObject(entry.customUnitReadingsJson)
                } catch (e: Exception) {
                    JSONObject()
                }
                put("custom_unit_readings_json", customJson)
                put("col_i_plant", entry.colI_plant)
                put("plant_remarks", entry.plantRemarks)
                put("issued_to_name", entry.issuedToName)
                put("issued_to_liters", entry.issuedToLiters)
                put("issued_to_remarks", entry.issuedToRemarks)
                put("col_j_issue_to", entry.colJ_issueTo)
                put("col_k_remarks", entry.colK_remarks)
                put("col_l_rate", entry.colL_rate)
                put("is_logged", entry.isLogged)
                put("is_synced", true)
                put("slip_attached", entry.slipAttached)
                put("logged_by", entry.loggedBy)
            }

            val request = newRequestBuilder("daily_logs?on_conflict=date_key")
                .addHeader("Prefer", "resolution=merge-duplicates")
                .post(json.toString().toRequestBody(JSON_MEDIA_TYPE))
                .build()

            client.newCall(request).execute().use { response ->
                response.isSuccessful || response.code in 200..299
            }
        } catch (e: Exception) {
            Log.e(TAG, "pushDailyLog error", e)
            false
        }
    }

    suspend fun pushAllDailyLogs(logs: List<DailyLogEntry>): Boolean = withContext(Dispatchers.IO) {
        try {
            val array = JSONArray()
            logs.forEach { entry ->
                val json = JSONObject().apply {
                    put("date_key", entry.dateKey)
                    put("day_number", entry.dayNumber)
                    put("month_year", entry.monthYear)
                    put("day_of_week", entry.dayOfWeek)
                    put("col_b_unit1", entry.colB_unit1)
                    put("col_c_unit2", entry.colC_unit2)
                    put("col_d_unit3", entry.colD_unit3)
                    put("col_e_unit4", entry.colE_unit4)
                    put("col_f_unit5", entry.colF_unit5)
                    put("col_g_unit6", entry.colG_unit6)
                    put("col_h_unit7", entry.colH_unit7)
                    val customJson = try {
                        JSONObject(entry.customUnitReadingsJson)
                    } catch (e: Exception) {
                        JSONObject()
                    }
                    put("custom_unit_readings_json", customJson)
                    put("col_i_plant", entry.colI_plant)
                    put("plant_remarks", entry.plantRemarks)
                    put("issued_to_name", entry.issuedToName)
                    put("issued_to_liters", entry.issuedToLiters)
                    put("issued_to_remarks", entry.issuedToRemarks)
                    put("col_j_issue_to", entry.colJ_issueTo)
                    put("col_k_remarks", entry.colK_remarks)
                    put("col_l_rate", entry.colL_rate)
                    put("is_logged", entry.isLogged)
                    put("is_synced", true)
                    put("slip_attached", entry.slipAttached)
                    put("logged_by", entry.loggedBy)
                }
                array.put(json)
            }

            val request = newRequestBuilder("daily_logs?on_conflict=date_key")
                .addHeader("Prefer", "resolution=merge-duplicates")
                .post(array.toString().toRequestBody(JSON_MEDIA_TYPE))
                .build()

            client.newCall(request).execute().use { response ->
                response.isSuccessful || response.code in 200..299
            }
        } catch (e: Exception) {
            Log.e(TAG, "pushAllDailyLogs error", e)
            false
        }
    }
}
