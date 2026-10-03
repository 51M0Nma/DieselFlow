package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val username: String,             // Unique login handle (e.g. "Simon-Mahajan")
    val password: String,             // Login password (e.g. "51M0N@P455w0rd")
    val fullName: String,             // Display Name (e.g. "Simon Mahajan")
    val role: String = "OPERATOR",    // "ADMIN", "MANAGER", "OPERATOR", "VIEWER"
    val canEditDailyLog: Boolean = true,
    val canViewDailyLog: Boolean = true,
    val canEditMonthlyGrid: Boolean = true,
    val canViewMonthlyGrid: Boolean = true,
    val canViewFleetSummary: Boolean = true,
    val canEditSettings: Boolean = false,
    val canManageUsers: Boolean = false,
    val canManageUnits: Boolean = false,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
) {
    val isAdmin: Boolean
        get() = role.equals("ADMIN", ignoreCase = true) || username.equals("Simon-Mahajan", ignoreCase = true)
}
