package org.lortodo.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.filled.ViewKanban
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import org.lortodo.domain.model.Priority
import org.lortodo.domain.model.Task
import org.lortodo.domain.repository.SmartView
import org.lortodo.ui.components.DateStrip
import org.lortodo.ui.components.QuickAddBar
import org.lortodo.ui.components.TaskItemCard
import org.lortodo.ui.components.TopHeader
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToTaskDetail: (Long) -> Unit,
    onNavigateToCalendar: () -> Unit,
    onNavigateToKanban: () -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToFocusTimer: () -> Unit,
    onNavigateToStatistics: () -> Unit,
    onNavigateToListsAndTags: () -> Unit,
    onNavigateToTemplates: () -> Unit = {},
    onNavigateToTrashArchive: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToAbout: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var showCompletedTasks by remember { mutableStateOf(true) }

    LaunchedEffect(uiState.userMessage) {
        uiState.userMessage?.let { msg ->
            val result = snackbarHostState.showSnackbar(
                message = msg,
                actionLabel = if (uiState.lastDeletedTask != null) "Undo" else null,
                duration = SnackbarDuration.Short
            )
            if (result == SnackbarResult.ActionPerformed) {
                viewModel.undoDelete()
            }
            viewModel.clearUserMessage()
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = !uiState.isSelectionMode,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier.width(300.dp)
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 20.dp)
                ) {
                    item {
                        Text(
                            text = "LOR TO-DO",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(vertical = 12.dp, horizontal = 12.dp)
                        )
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // Smart Views section
                    item {
                        Text("Smart Views", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.outline)
                        Spacer(modifier = Modifier.height(6.dp))
                    }

                    item {
                        DrawerSmartViewItem("Inbox", Icons.Default.Inbox, uiState.activeView == SmartView.INBOX) {
                            viewModel.selectSmartView(SmartView.INBOX)
                            scope.launch { drawerState.close() }
                        }
                        DrawerSmartViewItem("Today", Icons.Default.Today, uiState.activeView == SmartView.TODAY) {
                            viewModel.selectSmartView(SmartView.TODAY)
                            scope.launch { drawerState.close() }
                        }
                        DrawerSmartViewItem("Tomorrow", Icons.Default.Schedule, uiState.activeView == SmartView.TOMORROW) {
                            viewModel.selectSmartView(SmartView.TOMORROW)
                            scope.launch { drawerState.close() }
                        }
                        DrawerSmartViewItem("Upcoming (7 Days)", Icons.Default.CalendarMonth, uiState.activeView == SmartView.UPCOMING) {
                            viewModel.selectSmartView(SmartView.UPCOMING)
                            scope.launch { drawerState.close() }
                        }
                        DrawerSmartViewItem("Overdue", Icons.Default.Checklist, uiState.activeView == SmartView.OVERDUE) {
                            viewModel.selectSmartView(SmartView.OVERDUE)
                            scope.launch { drawerState.close() }
                        }
                        DrawerSmartViewItem("All Tasks", Icons.Default.CheckCircle, uiState.activeView == SmartView.ALL) {
                            viewModel.selectSmartView(SmartView.ALL)
                            scope.launch { drawerState.close() }
                        }
                        DrawerSmartViewItem("Starred", Icons.Default.Star, uiState.activeView == SmartView.STARRED) {
                            viewModel.selectSmartView(SmartView.STARRED)
                            scope.launch { drawerState.close() }
                        }
                        DrawerSmartViewItem("Completed", Icons.Default.Archive, uiState.activeView == SmartView.COMPLETED) {
                            viewModel.selectSmartView(SmartView.COMPLETED)
                            scope.launch { drawerState.close() }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Lists", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.outline)
                        Spacer(modifier = Modifier.height(4.dp))
                    }

                    // User Custom Lists
                    items(uiState.lists) { list ->
                        NavigationDrawerItem(
                            label = { Text(list.name) },
                            icon = {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(
                                            try { Color(android.graphics.Color.parseColor(list.colorHex)) }
                                            catch (_: Exception) { MaterialTheme.colorScheme.primary }
                                        )
                                )
                            },
                            selected = uiState.selectedListId == list.id,
                            onClick = {
                                viewModel.selectList(list.id)
                                scope.launch { drawerState.close() }
                            },
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }

                    // Saved Filters (F-028)
                    if (uiState.savedFilters.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            HorizontalDivider()
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Saved Filters", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.outline)
                            Spacer(modifier = Modifier.height(4.dp))
                        }

                        items(uiState.savedFilters) { filter ->
                            NavigationDrawerItem(
                                label = { Text(filter.name) },
                                icon = {
                                    Icon(
                                        imageVector = Icons.Default.FilterList,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                },
                                selected = false,
                                onClick = {
                                    viewModel.applySavedFilter(filter)
                                    scope.launch { drawerState.close() }
                                },
                                modifier = Modifier.padding(vertical = 2.dp)
                            )
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Features & Tools", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.outline)
                        Spacer(modifier = Modifier.height(4.dp))
                    }

                    // Tools & Productivity Views
                    item {
                        DrawerSmartViewItem("Calendar", Icons.Default.CalendarMonth, false) {
                            scope.launch { drawerState.close() }
                            onNavigateToCalendar()
                        }
                        DrawerSmartViewItem("Kanban Board", Icons.Default.ViewKanban, false) {
                            scope.launch { drawerState.close() }
                            onNavigateToKanban()
                        }
                        DrawerSmartViewItem("Focus Timer", Icons.Default.Timer, false) {
                            scope.launch { drawerState.close() }
                            onNavigateToFocusTimer()
                        }
                        DrawerSmartViewItem("Statistics", Icons.Default.BarChart, false) {
                            scope.launch { drawerState.close() }
                            onNavigateToStatistics()
                        }
                        DrawerSmartViewItem("Lists & Tags", Icons.Default.Folder, false) {
                            scope.launch { drawerState.close() }
                            onNavigateToListsAndTags()
                        }
                        DrawerSmartViewItem("Templates", Icons.Default.ContentCopy, false) {
                            scope.launch { drawerState.close() }
                            onNavigateToTemplates()
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // System items
                    item {
                        DrawerSmartViewItem("Trash & Archive", Icons.Default.Delete, uiState.activeView == SmartView.TRASH) {
                            scope.launch { drawerState.close() }
                            onNavigateToTrashArchive()
                        }
                        DrawerSmartViewItem("Settings", Icons.Default.Settings, false) {
                            scope.launch { drawerState.close() }
                            onNavigateToSettings()
                        }
                        DrawerSmartViewItem("About LOR TO-DO", Icons.Default.CheckCircle, false) {
                            scope.launch { drawerState.close() }
                            onNavigateToAbout()
                        }
                    }
                }
            }
        }
    ) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            bottomBar = {
                if (!uiState.isSelectionMode) {
                    QuickAddBar(
                        onAddTask = { parsed ->
                            viewModel.quickAddTask(parsed)
                        }
                    )
                }
            },
            floatingActionButton = {
                if (!uiState.isSelectionMode) {
                    FloatingActionButton(
                        onClick = { onNavigateToTaskDetail(0L) },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        shape = CircleShape,
                        modifier = Modifier.padding(bottom = 60.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Add Task")
                    }
                }
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(MaterialTheme.colorScheme.background)
            ) {
                // Header: Contextual Selection Bar or Normal TopHeader
                if (uiState.isSelectionMode) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        tonalElevation = 3.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = { viewModel.clearSelection() }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Cancel Selection",
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                            Text(
                                text = "${uiState.selectedTaskIds.size} selected",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = { viewModel.selectAllTasks(uiState.tasks) }) {
                                Icon(
                                    imageVector = Icons.Default.SelectAll,
                                    contentDescription = "Select All",
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                            IconButton(onClick = { viewModel.bulkSetCompleted(true) }) {
                                Icon(
                                    imageVector = Icons.Default.DoneAll,
                                    contentDescription = "Mark Selected Complete",
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                            IconButton(onClick = { viewModel.bulkDelete() }) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete Selected",
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                } else {
                    // Top Header (matching mockup: "Hi Ene", "What's up for today?")
                    TopHeader(
                        userName = uiState.preferences.userName,
                        subtitle = "What\'s up for today?",
                        onMenuClick = { scope.launch { drawerState.open() } },
                        onSearchClick = onNavigateToSearch,
                        onCalendarClick = onNavigateToCalendar,
                        onAvatarClick = onNavigateToSettings
                    )
                }

                // Date Strip (Mo Tu We Th Fr Sa Su with highlighted active day)
                if (uiState.activeView == SmartView.TODAY && !uiState.isSelectionMode) {
                    DateStrip(
                        selectedDateMillis = uiState.selectedDateMillis,
                        onDateSelected = { date -> viewModel.selectDate(date) }
                    )
                }

                // Section Title Row (e.g. "Today / Thur 8 May")
                val activeTasks = uiState.tasks.filter { !it.isCompleted }
                val completedTasks = uiState.tasks.filter { it.isCompleted }

                val headerTitle = when (uiState.activeView) {
                    SmartView.TODAY -> "Today"
                    SmartView.INBOX -> "Inbox"
                    SmartView.TOMORROW -> "Tomorrow"
                    SmartView.UPCOMING -> "Upcoming"
                    SmartView.OVERDUE -> "Overdue"
                    SmartView.ALL -> "All Tasks"
                    SmartView.COMPLETED -> "Completed"
                    SmartView.STARRED -> "Starred"
                    SmartView.NO_DATE -> "No Date"
                    SmartView.TRASH -> "Trash"
                    SmartView.ARCHIVE -> "Archive"
                }

                val dateSubtitle = SimpleDateFormat("EEE d MMM", Locale.getDefault()).format(Date(uiState.selectedDateMillis))

                Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = headerTitle,
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (uiState.activeView == SmartView.TODAY) {
                                Text(
                                    text = dateSubtitle,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Priority Filter Quick Chips
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            FilterChip(
                                selected = uiState.selectedPriorityFilter == null,
                                onClick = { viewModel.setPriorityFilter(null) },
                                label = { Text("All", fontSize = 11.sp) }
                            )
                            FilterChip(
                                selected = uiState.selectedPriorityFilter == Priority.HIGH,
                                onClick = {
                                    viewModel.setPriorityFilter(
                                        if (uiState.selectedPriorityFilter == Priority.HIGH) null else Priority.HIGH
                                    )
                                },
                                label = { Text("!High", fontSize = 11.sp) }
                            )
                        }
                    }
                }

                // Main Tasks List
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(top = 8.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (activeTasks.isEmpty() && completedTasks.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "No tasks yet",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Add one below to get started!",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                }
                            }
                        }
                    } else {
                        // Active Tasks
                        items(activeTasks, key = { it.id }) { task ->
                            TaskItemCard(
                                task = task,
                                onToggleComplete = { viewModel.toggleComplete(task) },
                                onClick = { onNavigateToTaskDetail(task.id) },
                                onDelete = { viewModel.deleteTask(task) },
                                onToggleStar = { viewModel.toggleStar(task) },
                                isSelected = uiState.selectedTaskIds.contains(task.id),
                                isSelectionMode = uiState.isSelectionMode,
                                onLongClick = { viewModel.toggleTaskSelection(task.id) }
                            )
                        }

                        // Completed Tasks Section
                        if (completedTasks.isNotEmpty()) {
                            item {
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { showCompletedTasks = !showCompletedTasks }
                                        .padding(vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Completed (${completedTasks.size})",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(
                                        imageVector = if (showCompletedTasks) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            if (showCompletedTasks) {
                                items(completedTasks, key = { it.id }) { task ->
                                    TaskItemCard(
                                        task = task,
                                        onToggleComplete = { viewModel.toggleComplete(task) },
                                        onClick = { onNavigateToTaskDetail(task.id) },
                                        onDelete = { viewModel.deleteTask(task) },
                                        onToggleStar = { viewModel.toggleStar(task) },
                                        isSelected = uiState.selectedTaskIds.contains(task.id),
                                        isSelectionMode = uiState.isSelectionMode,
                                        onLongClick = { viewModel.toggleTaskSelection(task.id) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DrawerSmartViewItem(
    title: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    NavigationDrawerItem(
        label = { Text(title) },
        icon = { Icon(imageVector = icon, contentDescription = null) },
        selected = isSelected,
        onClick = onClick,
        modifier = Modifier.padding(vertical = 2.dp)
    )
}
