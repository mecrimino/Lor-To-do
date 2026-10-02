package org.lortodo.ui.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.lortodo.ui.components.DueDatePickerSection
import org.lortodo.ui.components.PrioritySelector
import org.lortodo.ui.components.RecurrenceDialog
import org.lortodo.ui.components.SubtaskChecklist

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TaskDetailScreen(
    viewModel: TaskDetailViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var showRecurrenceDialog by remember { mutableStateOf(false) }
    var showNewTagDialog by remember { mutableStateOf(false) }
    var newTagName by remember { mutableStateOf("") }
    var showSaveTemplateDialog by remember { mutableStateOf(false) }
    var templateName by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (uiState.taskId == 0L) "New Task" else "Edit Task",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    // Star toggle
                    IconButton(onClick = { viewModel.toggleStarred() }) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Star",
                            tint = if (uiState.isStarred) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                        )
                    }

                    // Duplicate button (if existing task)
                    if (uiState.taskId != 0L) {
                        IconButton(onClick = { viewModel.duplicateTask { onNavigateBack() } }) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Duplicate Task"
                            )
                        }

                        // Save as Template button
                        IconButton(onClick = {
                            templateName = uiState.title
                            showSaveTemplateDialog = true
                        }) {
                            Icon(
                                imageVector = Icons.Default.BookmarkBorder,
                                contentDescription = "Save as Template"
                            )
                        }

                        // Delete button
                        IconButton(onClick = { viewModel.deleteTask(onNavigateBack) }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }

                    // Save Button
                    Button(
                        onClick = { viewModel.saveTask(onNavigateBack) },
                        enabled = uiState.title.isNotBlank(),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text("Save")
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
            // Title Input
            OutlinedTextField(
                value = uiState.title,
                onValueChange = { viewModel.updateTitle(it) },
                placeholder = {
                    Text(
                        "What needs to be done?",
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                textStyle = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                    unfocusedIndicatorColor = Color.Transparent
                )
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Notes Input
            OutlinedTextField(
                value = uiState.notes,
                onValueChange = { viewModel.updateNotes(it) },
                placeholder = { Text("Add notes, description, or markdown links...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                shape = RoundedCornerShape(12.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                    unfocusedIndicatorColor = Color.Transparent
                )
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Due Date & Time Section
            Text(
                text = "Due Date & Time",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            DueDatePickerSection(
                dueDateMillis = uiState.dueDate,
                dueTimeMillis = uiState.dueTime,
                isAllDay = uiState.isAllDay,
                onDateChanged = { viewModel.updateDueDate(it) },
                onTimeChanged = { time, allDay -> viewModel.updateDueTime(time, allDay) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Recurrence Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Repeat, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = uiState.recurrenceRule?.toReadableString() ?: "Does not repeat",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                TextButton(onClick = { showRecurrenceDialog = true }) {
                    Text(if (uiState.recurrenceRule != null) "Change" else "Repeat")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))

            // Priority Section
            Text(
                text = "Priority",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            PrioritySelector(
                selectedPriority = uiState.priority,
                onPrioritySelected = { viewModel.updatePriority(it) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // List Selector
            Text(
                text = "List",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                uiState.availableLists.forEach { list ->
                    FilterChip(
                        selected = uiState.listId == list.id,
                        onClick = { viewModel.updateListId(list.id) },
                        label = { Text(list.name) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Tags Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Tags",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                TextButton(onClick = { showNewTagDialog = true }) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("New Tag")
                }
            }
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                uiState.availableTags.forEach { tag ->
                    val isSelected = uiState.tags.any { it.id == tag.id }
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.toggleTag(tag) },
                        label = { Text("#${tag.name}") }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))

            // Subtasks Section
            SubtaskChecklist(
                subtasks = uiState.subtasks,
                onToggleSubtask = { viewModel.toggleSubtask(it) },
                onAddSubtask = { viewModel.addSubtask(it) },
                onDeleteSubtask = { viewModel.deleteSubtask(it) }
            )

            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))

            // Reminders Section
            Text(
                text = "Reminders",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                uiState.reminders.forEach { reminder ->
                    val offsetLabel = when (reminder.offsetMinutes) {
                        0 -> "At due time"
                        10 -> "10m before"
                        60 -> "1h before"
                        1440 -> "1 day before"
                        else -> "${reminder.offsetMinutes}m before"
                    }
                    AssistChip(
                        onClick = {},
                        label = { Text("⏰ $offsetLabel") },
                        trailingIcon = {
                            IconButton(
                                onClick = { viewModel.removeReminder(reminder) },
                                modifier = Modifier.size(16.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Remove")
                            }
                        }
                    )
                }
            }

            // Quick add reminder presets
            if (uiState.dueDate != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    SuggestionChip(
                        onClick = { viewModel.addReminderOffset(0) },
                        label = { Text("+ At time", fontSize = 11.sp) }
                    )
                    SuggestionChip(
                        onClick = { viewModel.addReminderOffset(10) },
                        label = { Text("+ 10m before", fontSize = 11.sp) }
                    )
                    SuggestionChip(
                        onClick = { viewModel.addReminderOffset(60) },
                        label = { Text("+ 1h before", fontSize = 11.sp) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }

    // Recurrence Dialog
    if (showRecurrenceDialog) {
        RecurrenceDialog(
            initialRule = uiState.recurrenceRule,
            onDismiss = { showRecurrenceDialog = false },
            onConfirm = { rule ->
                viewModel.updateRecurrenceRule(rule)
                showRecurrenceDialog = false
            }
        )
    }

    // New Tag Dialog
    if (showNewTagDialog) {
        AlertDialog(
            onDismissRequest = { showNewTagDialog = false },
            title = { Text("Create Tag") },
            text = {
                OutlinedTextField(
                    value = newTagName,
                    onValueChange = { newTagName = it },
                    label = { Text("Tag name") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newTagName.isNotBlank()) {
                            viewModel.addNewTag(newTagName.trim().removePrefix("#"))
                            newTagName = ""
                            showNewTagDialog = false
                        }
                    }
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewTagDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Save as Template Dialog
    if (showSaveTemplateDialog) {
        AlertDialog(
            onDismissRequest = { showSaveTemplateDialog = false },
            title = { Text("Save as Template") },
            text = {
                Column {
                    Text("Enter a name for this template:", style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = templateName,
                        onValueChange = { templateName = it },
                        label = { Text("Template Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (templateName.isNotBlank()) {
                            viewModel.saveAsTemplate(templateName) {
                                showSaveTemplateDialog = false
                            }
                        }
                    },
                    enabled = templateName.isNotBlank()
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSaveTemplateDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
