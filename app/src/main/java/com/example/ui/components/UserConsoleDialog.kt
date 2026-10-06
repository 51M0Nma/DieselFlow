package com.example.ui.components

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserEntity
import com.example.data.repository.DieselFlowRepository
import com.example.ui.theme.DieselError
import com.example.ui.theme.DieselOnPrimary
import com.example.ui.theme.DieselOnSecondaryContainer
import com.example.ui.theme.DieselOnSurface
import com.example.ui.theme.DieselOnSurfaceVariant
import com.example.ui.theme.DieselPrimary
import com.example.ui.theme.DieselSecondary
import com.example.ui.theme.DieselSecondaryContainer
import com.example.ui.theme.DieselSurfaceContainer
import com.example.ui.theme.DieselSurfaceContainerHighest
import com.example.ui.theme.DieselSurfaceContainerLow
import com.example.ui.theme.DieselSurfaceContainerLowest
import com.example.ui.theme.DieselTertiary
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserConsoleBottomSheet(
    users: List<UserEntity>,
    currentUser: UserEntity,
    repository: DieselFlowRepository,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var editingUser by remember { mutableStateOf<UserEntity?>(null) }
    var isAddingNew by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(DieselPrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AdminPanelSettings,
                            contentDescription = null,
                            tint = DieselOnPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "User Access & Permissions Console",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = DieselOnSurface
                        )
                        Text(
                            text = "Administered by ${currentUser.fullName}",
                            style = MaterialTheme.typography.bodySmall,
                            color = DieselSecondary
                        )
                    }
                }

                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = DieselOnSurfaceVariant
                    )
                }
            }

            if (editingUser == null && !isAddingNew) {
                // User List View
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "REGISTERED USERS (${users.size})",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = DieselSecondary
                    )

                    Button(
                        onClick = {
                            isAddingNew = true
                            editingUser = UserEntity(
                                id = 0,
                                username = "",
                                password = "",
                                fullName = "",
                                role = "CUSTOM",
                                canEditDailyLog = false,
                                canViewDailyLog = false,
                                canEditMonthlyGrid = false,
                                canViewMonthlyGrid = false,
                                canViewFleetSummary = false,
                                canEditSettings = false,
                                canManageUsers = false,
                                canManageUnits = false,
                                isActive = true
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = DieselPrimary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .height(34.dp)
                            .testTag("add_new_user_button")
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add User", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    users.forEach { user ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = DieselSurfaceContainerLowest),
                            shape = RoundedCornerShape(10.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (user.isAdmin) DieselPrimary else DieselSurfaceContainer
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = if (user.isAdmin) Icons.Default.AdminPanelSettings else Icons.Default.Person,
                                            contentDescription = null,
                                            tint = if (user.isAdmin) DieselOnPrimary else DieselSecondary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }

                                    Column {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text(
                                                text = user.fullName.ifEmpty { user.username },
                                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                                color = DieselOnSurface
                                            )

                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(if (user.isAdmin) DieselSecondaryContainer else DieselSurfaceContainer)
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = user.role.uppercase(),
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.Bold
                                                    ),
                                                    color = if (user.isAdmin) DieselOnSecondaryContainer else DieselSecondary
                                                )
                                            }
                                        }

                                        Text(
                                            text = "@${user.username} • ${if (user.isActive) "Active" else "Disabled"}",
                                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                            color = if (user.isActive) DieselTertiary else DieselError
                                        )

                                        // Permission summary badges
                                        Row(
                                            modifier = Modifier.padding(top = 2.dp),
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            if (user.canEditDailyLog) PermBadge("Log Edit") else if (user.canViewDailyLog) PermBadge("Log View")
                                            if (user.canEditMonthlyGrid) PermBadge("Grid Edit") else if (user.canViewMonthlyGrid) PermBadge("Grid View")
                                            if (user.canManageUnits) PermBadge("Units CRUD")
                                            if (user.canManageUsers) PermBadge("Users Admin")
                                        }
                                    }
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    IconButton(
                                        onClick = {
                                            isAddingNew = false
                                            editingUser = user
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "Edit User",
                                            tint = DieselPrimary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }

                                    if (!user.username.equals("Simon-Mahajan", ignoreCase = true)) {
                                        IconButton(
                                            onClick = {
                                                coroutineScope.launch {
                                                    repository.deleteUser(user.id)
                                                    Toast.makeText(context, "User @${user.username} removed", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Delete User",
                                                tint = DieselError,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // User Edit / Create Form
                val userToEdit = editingUser!!
                var editUsername by remember { mutableStateOf(userToEdit.username) }
                var editPassword by remember { mutableStateOf(userToEdit.password) }
                var editFullName by remember { mutableStateOf(userToEdit.fullName) }
                var editRole by remember { mutableStateOf(userToEdit.role) }
                var canEditDailyLog by remember { mutableStateOf(userToEdit.canEditDailyLog) }
                var canViewDailyLog by remember { mutableStateOf(userToEdit.canViewDailyLog) }
                var canEditMonthlyGrid by remember { mutableStateOf(userToEdit.canEditMonthlyGrid) }
                var canViewMonthlyGrid by remember { mutableStateOf(userToEdit.canViewMonthlyGrid) }
                var canViewFleetSummary by remember { mutableStateOf(userToEdit.canViewFleetSummary) }
                var canEditSettings by remember { mutableStateOf(userToEdit.canEditSettings) }
                var canManageUsers by remember { mutableStateOf(userToEdit.canManageUsers) }
                var canManageUnits by remember { mutableStateOf(userToEdit.canManageUnits) }
                var isActive by remember { mutableStateOf(userToEdit.isActive) }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DieselSurfaceContainerLowest),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = if (isAddingNew) "Add New App User" else "Edit User: ${userToEdit.username}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = DieselOnSurface
                        )

                        OutlinedTextField(
                            value = editFullName,
                            onValueChange = { editFullName = it },
                            label = { Text("Full Name") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = editUsername,
                            onValueChange = { editUsername = it },
                            label = { Text("Username / Handle") },
                            singleLine = true,
                            enabled = !userToEdit.username.equals("Simon-Mahajan", ignoreCase = true),
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = editPassword,
                            onValueChange = { editPassword = it },
                            label = { Text("Password") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Role Selector Quick Chips
                        Text(
                            text = "ASSIGN ROLE TEMPLATE",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = DieselSecondary
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            listOf("ADMIN", "MANAGER", "OPERATOR", "VIEWER", "CUSTOM").forEach { roleName ->
                                val isSelected = editRole.equals(roleName, ignoreCase = true)
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (isSelected) DieselPrimary else DieselSurfaceContainer)
                                        .clickable {
                                            editRole = roleName
                                            when (roleName) {
                                                "ADMIN" -> {
                                                    canEditDailyLog = true
                                                    canViewDailyLog = true
                                                    canEditMonthlyGrid = true
                                                    canViewMonthlyGrid = true
                                                    canViewFleetSummary = true
                                                    canEditSettings = true
                                                    canManageUsers = true
                                                    canManageUnits = true
                                                }
                                                "MANAGER" -> {
                                                    canEditDailyLog = true
                                                    canViewDailyLog = true
                                                    canEditMonthlyGrid = true
                                                    canViewMonthlyGrid = true
                                                    canViewFleetSummary = true
                                                    canEditSettings = true
                                                    canManageUsers = false
                                                    canManageUnits = true
                                                }
                                                "OPERATOR" -> {
                                                    canEditDailyLog = true
                                                    canViewDailyLog = true
                                                    canEditMonthlyGrid = false
                                                    canViewMonthlyGrid = true
                                                    canViewFleetSummary = true
                                                    canEditSettings = false
                                                    canManageUsers = false
                                                    canManageUnits = false
                                                }
                                                "VIEWER" -> {
                                                    canEditDailyLog = false
                                                    canViewDailyLog = true
                                                    canEditMonthlyGrid = false
                                                    canViewMonthlyGrid = true
                                                    canViewFleetSummary = true
                                                    canEditSettings = false
                                                    canManageUsers = false
                                                    canManageUnits = false
                                                }
                                                "CUSTOM" -> {
                                                    // Retains whatever checkboxes Admin manually picks
                                                }
                                            }
                                        }
                                        .padding(vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = roleName,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 8.5.sp
                                        ),
                                        color = if (isSelected) DieselOnPrimary else DieselOnSurface
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Custom Screen-by-Screen Permissions
                        Text(
                            text = "CUSTOM SCREEN ACCESS PERMISSIONS",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = DieselSecondary
                        )

                        Card(
                            colors = CardDefaults.cardColors(containerColor = DieselSurfaceContainerLow),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(10.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                PermCheckRow("Daily Log: Can View Screen", canViewDailyLog) { canViewDailyLog = it }
                                PermCheckRow("Daily Log: Can Edit Fuel Slips", canEditDailyLog) { canEditDailyLog = it }
                                PermCheckRow("Monthly Grid: Can View Spreadsheet", canViewMonthlyGrid) { canViewMonthlyGrid = it }
                                PermCheckRow("Monthly Grid: Can Edit Rates & Slips", canEditMonthlyGrid) { canEditMonthlyGrid = it }
                                PermCheckRow("Fleet Summary: Can View Analytics", canViewFleetSummary) { canViewFleetSummary = it }
                                PermCheckRow("Settings & Rates: Can Change Rate & Sheet", canEditSettings) { canEditSettings = it }
                                PermCheckRow("Fleet Unit Console: Can Add/Delete Units", canManageUnits) { canManageUnits = it }
                                PermCheckRow("User Console: Can Administer Users", canManageUsers) { canManageUsers = it }
                            }
                        }

                        // Active Account Toggle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Account Active / Enabled",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = DieselOnSurface
                            )
                            Switch(
                                checked = isActive,
                                onCheckedChange = { isActive = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = DieselOnPrimary,
                                    checkedTrackColor = DieselTertiary
                                )
                            )
                        }

                        // Form Actions
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            TextButton(
                                onClick = {
                                    editingUser = null
                                    isAddingNew = false
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Cancel", color = DieselSecondary)
                            }

                            Button(
                                onClick = {
                                    if (editUsername.isBlank() || editPassword.isBlank()) {
                                        Toast.makeText(context, "Username and Password required", Toast.LENGTH_SHORT).show()
                                        return@Button
                                    }
                                    val updated = userToEdit.copy(
                                        username = editUsername.trim(),
                                        password = editPassword,
                                        fullName = editFullName.trim().ifEmpty { editUsername },
                                        role = editRole,
                                        canEditDailyLog = canEditDailyLog,
                                        canViewDailyLog = canViewDailyLog,
                                        canEditMonthlyGrid = canEditMonthlyGrid,
                                        canViewMonthlyGrid = canViewMonthlyGrid,
                                        canViewFleetSummary = canViewFleetSummary,
                                        canEditSettings = canEditSettings,
                                        canManageUsers = canManageUsers,
                                        canManageUnits = canManageUnits,
                                        isActive = isActive
                                    )

                                    coroutineScope.launch {
                                        repository.saveUser(updated)
                                        editingUser = null
                                        isAddingNew = false
                                        Toast.makeText(context, "User @${updated.username} saved successfully", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = DieselPrimary),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("save_user_button")
                            ) {
                                Text("Save User", color = DieselOnPrimary, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PermBadge(label: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(3.dp))
            .background(DieselSurfaceContainerHighest)
            .padding(horizontal = 4.dp, vertical = 1.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp, fontWeight = FontWeight.SemiBold),
            color = DieselSecondary
        )
    }
}

@Composable
fun PermCheckRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall,
            color = DieselOnSurface
        )
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(checkedColor = DieselPrimary)
        )
    }
}
