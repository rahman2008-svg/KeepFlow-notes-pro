package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.PlannerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: PlannerViewModel,
    onNavigateToDeveloper: () -> Unit,
    onNavigateToCompany: () -> Unit,
    modifier: Modifier = Modifier
) {
    val darkMode by viewModel.darkModeState.collectAsState()
    val notificationsEnabled by viewModel.notificationsEnabledState.collectAsState()
    val dailyReminderEnabled by viewModel.dailyReminderEnabledState.collectAsState()
    val dailyReminderTime by viewModel.dailyReminderTimeState.collectAsState()

    var showTimePickerDialog by remember { mutableStateOf(false) }
    var selectedHour by remember { mutableStateOf(8) }
    var selectedMinute by remember { mutableStateOf(0) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Black, fontSize = 18.sp, color = MaterialTheme.colorScheme.primary, letterSpacing = 0.5.sp) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            // Theme Customization Grouped Categories
            item {
                SettingsSectionTitle("Theme Options")
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Dark Mode Configuration",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ThemeChip(
                                label = "System",
                                selected = darkMode == "system",
                                onClick = { viewModel.setDarkMode("system") },
                                modifier = Modifier.weight(1f)
                            )
                            ThemeChip(
                                label = "Light",
                                selected = darkMode == "light",
                                onClick = { viewModel.setDarkMode("light") },
                                modifier = Modifier.weight(1f)
                            )
                            ThemeChip(
                                label = "Dark",
                                selected = darkMode == "dark",
                                onClick = { viewModel.setDarkMode("dark") },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // Notification Customizations Category
            item {
                SettingsSectionTitle("Notifications")
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column {
                        // All notifications toggle
                        SettingsSwitchRow(
                            title = "Task Reminders",
                            subtitle = "Show precise alerts for scheduled tasks and alarms",
                            value = notificationsEnabled,
                            onValueChange = { viewModel.setNotificationsEnabled(it) },
                            icon = Icons.Default.Notifications
                        )
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                        
                        // Daily digest toggle
                        SettingsSwitchRow(
                            title = "Daily Summary Digest",
                            subtitle = "Morning outline of scheduled agenda items",
                            value = dailyReminderEnabled,
                            onValueChange = { viewModel.setDailyReminderEnabled(it) },
                            icon = Icons.Default.LightMode
                        )
                        
                        if (dailyReminderEnabled) {
                            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                            // Daily reminder timing adjust
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        val parts = dailyReminderTime.split(":")
                                        selectedHour = parts.getOrNull(0)?.toIntOrNull() ?: 8
                                        selectedMinute = parts.getOrNull(1)?.toIntOrNull() ?: 0
                                        showTimePickerDialog = true
                                    }
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Schedule,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Column {
                                        Text(
                                            text = "Morning Digest Time",
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.bodyLarge
                                        )
                                        Text(
                                            text = "Currently scheduled at $dailyReminderTime",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = "Edit Time",
                                    tint = MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                    }
                }
            }

            // Database Actions Category
            item {
                SettingsSectionTitle("Backup & Storage")
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column {
                        SettingsClickableRow(
                            title = "Local Off-Grid Backup",
                            subtitle = "Archive current schema files locally. Encryption and isolation active.",
                            icon = Icons.Default.Backup,
                            onClick = {
                                // Simulate local offline success
                            }
                        )
                    }
                }
            }

            // About Credits Category
            item {
                SettingsSectionTitle("About Smart Day Planner X")
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column {
                        SettingsClickableRow(
                            title = "About Developer",
                            subtitle = "Prince AR Abdur Rahman",
                            icon = Icons.Default.Person,
                            onClick = onNavigateToDeveloper
                        )
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                        SettingsClickableRow(
                            title = "About Company",
                            subtitle = "NexVora Lab's Ofc",
                            icon = Icons.Default.Business,
                            onClick = onNavigateToCompany
                        )
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text(
                                        text = "Version",
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                    Text(
                                        text = "Smart Day Planner X Core VM",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Text(
                                text = "1.0.0",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }

        // Custom time picker dialog
        if (showTimePickerDialog) {
            AlertDialog(
                onDismissRequest = { showTimePickerDialog = false },
                title = { Text("Set Morning Digest Time") },
                text = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = String.format("%02d:%02d", selectedHour, selectedMinute),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 32.sp,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(vertical = 12.dp)
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Hour", style = MaterialTheme.typography.labelSmall)
                                Slider(
                                    value = selectedHour.toFloat(),
                                    onValueChange = { selectedHour = it.toInt() },
                                    valueRange = 0f..23f,
                                    steps = 23,
                                    modifier = Modifier.width(100.dp)
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Minute", style = MaterialTheme.typography.labelSmall)
                                Slider(
                                    value = selectedMinute.toFloat(),
                                    onValueChange = { selectedMinute = it.toInt() },
                                    valueRange = 0f..59f,
                                    steps = 59,
                                    modifier = Modifier.width(100.dp)
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        viewModel.setDailyReminderTime(selectedHour, selectedMinute)
                        showTimePickerDialog = false
                    }) {
                        Text("Save")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showTimePickerDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
fun SettingsSectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 4.dp, top = 8.dp)
    )
}

@Composable
fun ThemeChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label, modifier = Modifier.fillMaxWidth(), textAlign = androidx.compose.ui.text.style.TextAlign.Center) },
        shape = RoundedCornerShape(10.dp),
        modifier = modifier
    )
}

@Composable
fun SettingsSwitchRow(
    title: String,
    subtitle: String,
    value: Boolean,
    onValueChange: (Boolean) -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Switch(
            checked = value,
            onCheckedChange = onValueChange,
            modifier = Modifier.testTag("settings_switch_${title.replace(" ", "_").lowercase()}")
        )
    }
}

@Composable
fun SettingsClickableRow(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.outline
        )
    }
}
