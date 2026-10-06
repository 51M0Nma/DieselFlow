package com.example.data.remote

import android.content.Context
import android.util.Log
import com.example.data.model.DailyLogEntry
import com.example.data.model.FleetAssetConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object GoogleSheetsService {
    private const val TAG = "GoogleSheetsService"
    private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()

    private const val PREFS_NAME = "google_sheets_prefs"
    private const val KEY_SPREADSHEET_ID = "active_spreadsheet_id"
    private const val KEY_SPREADSHEET_URL = "active_spreadsheet_url"

    const val DEFAULT_SPREADSHEET_ID = "1UAq4e6yEDR6I3o5IRRm6-8GmbYAXGLonoXr9prc7H88"
    const val DEFAULT_SPREADSHEET_URL = "https://docs.google.com/spreadsheets/d/1UAq4e6yEDR6I3o5IRRm6-8GmbYAXGLonoXr9prc7H88/edit?usp=sharing"

    private val client = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .writeTimeout(20, TimeUnit.SECONDS)
        .build()

    private var cachedSpreadsheetId: String? = DEFAULT_SPREADSHEET_ID
    private var cachedSpreadsheetUrl: String? = DEFAULT_SPREADSHEET_URL
    private var authToken: String? = null

    fun setAuthToken(token: String) {
        authToken = token
    }

    fun getSpreadsheetUrl(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val saved = prefs.getString(KEY_SPREADSHEET_URL, null)
        return saved ?: DEFAULT_SPREADSHEET_URL
    }

    fun getSpreadsheetId(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val saved = prefs.getString(KEY_SPREADSHEET_ID, null)
        return if (!saved.isNullOrEmpty()) saved else DEFAULT_SPREADSHEET_ID
    }

    fun saveSpreadsheetInfo(context: Context, id: String, url: String) {
        cachedSpreadsheetId = id
        cachedSpreadsheetUrl = url
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putString(KEY_SPREADSHEET_ID, id)
            .putString(KEY_SPREADSHEET_URL, url)
            .apply()
    }

    // Creates the complete Google Spreadsheet structure with 13 columns (A to M)
    suspend fun createOrInitializeSpreadsheet(
        context: Context,
        token: String?,
        assets: List<FleetAssetConfig>,
        logs: List<DailyLogEntry>
    ): Result<String> = withContext(Dispatchers.IO) {
        val bearer = token ?: authToken
        if (bearer.isNullOrEmpty()) {
            // Local offline mock/preview URL
            val defaultUrl = getSpreadsheetUrl(context)
            return@withContext Result.success(defaultUrl)
        }

        try {
            // 1. Create Spreadsheet via Google Sheets API v4
            val createBody = JSONObject().apply {
                put("properties", JSONObject().apply {
                    put("title", "DieselFlow Fuel Tracker 2026 (Live Terminal)")
                })
                val sheetsArray = JSONArray().apply {
                    put(JSONObject().apply {
                        put("properties", JSONObject().apply {
                            put("title", "Sheet1")
                            put("gridProperties", JSONObject().apply {
                                put("rowCount", 40)
                                put("columnCount", 16)
                                put("frozenRowCount", 1)
                            })
                        })
                    })
                }
                put("sheets", sheetsArray)
            }

            val createReq = Request.Builder()
                .url("https://sheets.googleapis.com/v4/spreadsheets")
                .addHeader("Authorization", "Bearer $bearer")
                .addHeader("Content-Type", "application/json")
                .post(createBody.toString().toRequestBody(JSON_MEDIA_TYPE))
                .build()

            val response = client.newCall(createReq).execute()
            if (!response.isSuccessful) {
                val errBody = response.body?.string() ?: ""
                Log.w(TAG, "Spreadsheet creation failed code: ${response.code}, msg: $errBody")
                return@withContext Result.failure(Exception("Failed to create spreadsheet: HTTP ${response.code}"))
            }

            val respJson = JSONObject(response.body?.string() ?: "{}")
            val newSheetId = respJson.getString("spreadsheetId")
            val sheetUrl = respJson.optString("spreadsheetUrl", "https://docs.google.com/spreadsheets/d/$newSheetId/edit")

            saveSpreadsheetInfo(context, newSheetId, sheetUrl)

            // 2. Initialize Headers and Rows
            populateFullSheet(bearer, newSheetId, assets, logs)

            Result.success(sheetUrl)
        } catch (e: Exception) {
            Log.e(TAG, "createOrInitializeSpreadsheet error", e)
            Result.failure(e)
        }
    }

    // Populate Headers (Row 1) and all 31 day rows (Rows 2 to 32) + Summary Totals (Row 33)
    suspend fun populateFullSheet(
        token: String,
        spreadsheetId: String,
        assets: List<FleetAssetConfig>,
        logs: List<DailyLogEntry>
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val valuesArray = JSONArray()

            // Header Row (Row 1)
            val header = JSONArray().apply {
                put("Date / Day (Col A)")
                put("Unit 1 (Col B)")
                put("Unit 2 (Col C)")
                put("Unit 3 (Col D)")
                put("Unit 4 (Col E)")
                put("Unit 5 (Col F)")
                put("Unit 6 (Col G)")
                put("Unit 7 (Col H)")
                put("Facility & Plant (Col I)")
                put("Issued To / Destination (Col J)")
                put("Shift Remarks (Col K)")
                put("Rate ₹/L (Col L)")
                put("Calculated Cost INR (Col M)")
            }
            valuesArray.put(header)

            // Rows 2 to 32 (Days 1 to 31)
            logs.sortedBy { it.dayNumber }.forEach { entry ->
                val rowNum = entry.targetRow
                val row = JSONArray().apply {
                    put("${entry.dayNumber}-Oct-2026 (${entry.dayOfWeek})")
                    put(if (entry.colB_unit1 > 0) entry.colB_unit1 else 0)
                    put(if (entry.colC_unit2 > 0) entry.colC_unit2 else 0)
                    put(if (entry.colD_unit3 > 0) entry.colD_unit3 else 0)
                    put(if (entry.colE_unit4 > 0) entry.colE_unit4 else 0)
                    put(if (entry.colF_unit5 > 0) entry.colF_unit5 else 0)
                    put(if (entry.colG_unit6 > 0) entry.colG_unit6 else 0)
                    put(if (entry.colH_unit7 > 0) entry.colH_unit7 else 0)
                    put(if (entry.colI_plant > 0) entry.colI_plant else 0)
                    put(if (entry.issuedToLiters > 0) "${entry.issuedToName} (${entry.issuedToLiters}L)" else entry.issuedToName.ifEmpty { "-" })
                    put(entry.colK_remarks.ifEmpty { "-" })
                    put(entry.colL_rate)
                    put("=(SUM(B$rowNum:I$rowNum))*L$rowNum")
                }
                valuesArray.put(row)
            }

            // Row 33: Monthly Summary Row
            val totalRow = JSONArray().apply {
                put("TOTALS (OCTOBER 2026)")
                put("=SUM(B2:B32)")
                put("=SUM(C2:C32)")
                put("=SUM(D2:D32)")
                put("=SUM(E2:E32)")
                put("=SUM(F2:F32)")
                put("=SUM(G2:G32)")
                put("=SUM(H2:H32)")
                put("=SUM(I2:I32)")
                put("TOTAL DISPENSED")
                put("-")
                put("-")
                put("=SUM(M2:M32)")
            }
            valuesArray.put(totalRow)

            val updateBody = JSONObject().apply {
                put("range", "Sheet1!A1:M33")
                put("majorDimension", "ROWS")
                put("values", valuesArray)
            }

            val updateReq = Request.Builder()
                .url("https://sheets.googleapis.com/v4/spreadsheets/$spreadsheetId/values/Sheet1!A1:M33?valueInputOption=USER_ENTERED")
                .addHeader("Authorization", "Bearer $token")
                .addHeader("Content-Type", "application/json")
                .put(updateBody.toString().toRequestBody(JSON_MEDIA_TYPE))
                .build()

            val updateResp = client.newCall(updateReq).execute()
            updateResp.isSuccessful
        } catch (e: Exception) {
            Log.e(TAG, "populateFullSheet error", e)
            false
        }
    }

    // Updates a single row in Google Sheets whenever a fuel slip is edited
    suspend fun updateRow(
        context: Context,
        token: String?,
        entry: DailyLogEntry
    ): Boolean = withContext(Dispatchers.IO) {
        val bearer = token ?: authToken
        val sheetId = getSpreadsheetId(context)
        if (bearer.isNullOrEmpty() || sheetId.isEmpty()) {
            return@withContext true
        }

        try {
            val rowNum = entry.targetRow
            val range = "Sheet1!A$rowNum:M$rowNum"

            val rowValues = JSONArray().apply {
                put("${entry.dayNumber}-Oct-2026 (${entry.dayOfWeek})")
                put(if (entry.colB_unit1 > 0) entry.colB_unit1 else 0)
                put(if (entry.colC_unit2 > 0) entry.colC_unit2 else 0)
                put(if (entry.colD_unit3 > 0) entry.colD_unit3 else 0)
                put(if (entry.colE_unit4 > 0) entry.colE_unit4 else 0)
                put(if (entry.colF_unit5 > 0) entry.colF_unit5 else 0)
                put(if (entry.colG_unit6 > 0) entry.colG_unit6 else 0)
                put(if (entry.colH_unit7 > 0) entry.colH_unit7 else 0)
                put(if (entry.colI_plant > 0) entry.colI_plant else 0)
                put(if (entry.issuedToLiters > 0) "${entry.issuedToName} (${entry.issuedToLiters}L)" else entry.issuedToName.ifEmpty { "-" })
                put(entry.colK_remarks.ifEmpty { "-" })
                put(entry.colL_rate)
                put("=(SUM(B$rowNum:I$rowNum))*L$rowNum")
            }

            val body = JSONObject().apply {
                put("range", range)
                put("majorDimension", "ROWS")
                put("values", JSONArray().apply { put(rowValues) })
            }

            val req = Request.Builder()
                .url("https://sheets.googleapis.com/v4/spreadsheets/$sheetId/values/$range?valueInputOption=USER_ENTERED")
                .addHeader("Authorization", "Bearer $bearer")
                .addHeader("Content-Type", "application/json")
                .put(body.toString().toRequestBody(JSON_MEDIA_TYPE))
                .build()

            val resp = client.newCall(req).execute()
            resp.isSuccessful
        } catch (e: Exception) {
            Log.e(TAG, "updateRow error", e)
            false
        }
    }
}
