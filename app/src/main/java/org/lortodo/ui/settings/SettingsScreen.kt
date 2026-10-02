package org.lortodo.ui.settings

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import org.lortodo.domain.model.AccentColor
import org.lortodo.domain.model.AppTheme

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToAbout: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    var showResetDialog by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.message) {
        uiState.message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    // Share / Export JSON Launcher
    val shareIntent = { text: String, title: String ->
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, text)
            type = "text/plain"
        }
        context.startActivity(Intent.createChooser(sendIntent, title))
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToAbout) {
                        Icon(imageVector = Icons.Default.Info, contentDescription = "About")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            // Appearance Section
            SettingsSectionHeader("Appearance")

            Text("Theme", style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AppTheme.entries.forEach { theme ->
                    FilterChip(
                        selected = uiState.preferences.theme == theme,
                        onClick = { viewModel.updateTheme(theme) },
                        label = { Text(theme.name.lowercase().replaceFirstChar { it.uppercase() }) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text("Accent Color", style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(6.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                AccentColor.entries.forEach { accent ->
                    val isSelected = uiState.preferences.accentColor == accent
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.updateAccentColor(accent) },
                        label = { Text(accent.displayName) },
                        leadingIcon = {
                            if (accent.colorHex.isNotBlank()) {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .clip(CircleShape)
                                        .background(Color(android.graphics.Color.parseColor(accent.colorHex)))
                                )
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))

            // Privacy & Security Section
            SettingsSectionHeader("Privacy & Security")

            SettingsSwitchRow(
                title = "App Lock (Biometrics / PIN)",
                subtitle = "Require authentication to open app",
                checked = uiState.preferences.appLockEnabled,
                onCheckedChange = { viewModel.toggleAppLock(it) }
            )

            SettingsSwitchRow(
                title = "Hide in Recents",
                subtitle = "Prevent screenshots and hide content in app switcher",
                checked = uiState.preferences.hideInRecents,
                onCheckedChange = { viewModel.toggleHideInRecents(it) }
            )

            SettingsSwitchRow(
                title = "Hide Notification Content",
                subtitle = "Show generic 'Task reminder' on lock screen",
                checked = uiState.preferences.hideSensitiveNotifications,
                onCheckedChange = { viewModel.toggleHideSensitiveNotifications(it) }
            )

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))

            // Notifications & Reminders Section
            SettingsSectionHeader("Notifications & Reminders")

            SettingsSwitchRow(
                title = "Daily Summary",
                subtitle = "Morning digest of today's tasks (9:00 AM)",
                checked = uiState.preferences.dailySummaryEnabled,
                onCheckedChange = { viewModel.toggleDailySummary(it) }
            )

            SettingsSwitchRow(
                title = "Overdue Nudges",
                subtitle = "Remind about tasks that passed their due date",
                checked = uiState.preferences.overdueNudgesEnabled,
                onCheckedChange = { viewModel.toggleOverdueNudges(it) }
            )

            SettingsSwitchRow(
                title = "Sound",
                subtitle = "Play notification sound",
                checked = uiState.preferences.soundEnabled,
                onCheckedChange = { viewModel.toggleSound(it) }
            )

            SettingsSwitchRow(
                title = "Haptics & Vibration",
                subtitle = "Vibrate on task completion and alarms",
                checked = uiState.preferences.hapticsEnabled,
                onCheckedChange = { viewModel.toggleHaptics(it) }
            )

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))

            // Backup & Data Ownership Section
            SettingsSectionHeader("Data Ownership & Backups")
            Text(
                text = "Export and import your data in open formats. Everything stays on your device.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = {
                        scope.launch {
                            val json = viewModel.exportJsonData()
                            shareIntent(json, "Export LOR TO-DO Backup (JSON)")
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Export JSON")
                }

                OutlinedButton(
                    onClick = {
                        scope.launch {
                            val csv = viewModel.exportCsvData()
                            shareIntent(csv, "Export Tasks (CSV)")
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Export CSV")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = {
                    scope.launch {
                        val md = viewModel.exportMarkdownData()
                        shareIntent(md, "Export Tasks (Markdown)")
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Export as Markdown")
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))

            // Danger Zone / Reset
            SettingsSectionHeader("Danger Zone")
            OutlinedButton(
                onClick = { showResetDialog = true },
                modifier = Modifier.fillMaxWidth(),
                colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Clear All Data & Reset App")
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Reset All Data?") },
            text = { Text("This will permanently delete all tasks, lists, tags, and settings. This cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearAllData()
                        showResetDialog = false
                    }
                ) {
                    Text("Delete Everything", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 17.sp),
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@Composable
private fun SettingsSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium))
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(modifier = Modifier.width(12.dp))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
