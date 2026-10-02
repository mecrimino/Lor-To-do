package org.lortodo.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.lortodo.core.AppContainer
import org.lortodo.domain.engine.ParsedTaskResult
import org.lortodo.domain.engine.RecurrenceEngine
import org.lortodo.domain.model.Priority
import org.lortodo.domain.model.Reminder
import org.lortodo.domain.model.SavedFilter
import org.lortodo.domain.model.Task
import org.lortodo.domain.repository.SmartView
import org.lortodo.domain.repository.SortDirection
import org.lortodo.domain.repository.TaskFilter
import org.lortodo.domain.repository.TaskSortBy
import java.util.Calendar

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModel(
    private val appContainer: AppContainer
) : ViewModel() {

    private val taskRepository = appContainer.taskRepository
    private val taskListRepository = appContainer.taskListRepository
    private val tagRepository = appContainer.tagRepository
    private val reminderRepository = appContainer.reminderRepository
    private val userPreferencesRepository = appContainer.userPreferencesRepository
    private val savedFilterRepository = appContainer.savedFilterRepository
    private val alarmScheduler = appContainer.alarmScheduler

    private val _activeView = MutableStateFlow(SmartView.TODAY)
    private val _selectedDateMillis = MutableStateFlow(getStartOfTodayMillis())
    private val _selectedPriority = MutableStateFlow<Priority?>(null)
    private val _selectedListId = MutableStateFlow<Long?>(null)
    private val _sortBy = MutableStateFlow(TaskSortBy.MANUAL)
    private val _sortDirection = MutableStateFlow(SortDirection.ASCENDING)
    private val _selectedTaskIds = MutableStateFlow<Set<Long>>(emptySet())
    private val _lastDeletedTask = MutableStateFlow<Task?>(null)
    private val _userMessage = MutableStateFlow<String?>(null)

    private data class FilterConfig(
        val activeView: SmartView,
        val selectedDateMillis: Long,
        val selectedPriority: Priority?,
        val selectedListId: Long?,
        val sortBy: TaskSortBy,
        val sortDirection: SortDirection
    )

    private val filterConfigFlow = combine(
        _activeView,
        _selectedDateMillis,
        _selectedPriority,
        _selectedListId
    ) { activeView, selectedDate, priority, listId ->
        FilterConfig(activeView, selectedDate, priority, listId, _sortBy.value, _sortDirection.value)
    }.combine(_sortBy) { config, sortBy ->
        config.copy(sortBy = sortBy)
    }.combine(_sortDirection) { config, sortDirection ->
        config.copy(sortDirection = sortDirection)
    }

    private val baseUiState = combine(
        filterConfigFlow,
        taskListRepository.getAllLists(),
        userPreferencesRepository.userPreferences,
        _selectedTaskIds
    ) { config, lists, prefs, selectedIds ->
        HomeUiState(
            activeView = config.activeView,
            selectedDateMillis = config.selectedDateMillis,
            selectedPriorityFilter = config.selectedPriority,
            selectedListId = config.selectedListId,
            sortBy = config.sortBy,
            sortDirection = config.sortDirection,
            lists = lists,
            preferences = prefs,
            selectedTaskIds = selectedIds
        )
    }.combine(savedFilterRepository.getAllFilters()) { state, filters ->
        state.copy(savedFilters = filters)
    }.combine(_lastDeletedTask) { state, lastDeleted ->
        state.copy(lastDeletedTask = lastDeleted)
    }.combine(_userMessage) { state, msg ->
        state.copy(userMessage = msg)
    }

    val uiState: StateFlow<HomeUiState> = baseUiState.flatMapLatest { state ->
        val filter = TaskFilter(
            listId = state.selectedListId,
            priority = state.selectedPriorityFilter
        )
        taskRepository.getTasksBySmartView(
            view = state.activeView,
            filter = filter,
            sortBy = state.sortBy,
            sortDirection = state.sortDirection
        ).combine(taskRepository.getTasksForDate(state.selectedDateMillis)) { smartViewTasks, dateTasks ->
            val tasksToShow = if (state.activeView == SmartView.TODAY && state.selectedDateMillis != getStartOfTodayMillis()) {
                dateTasks
            } else {
                smartViewTasks
            }
            state.copy(tasks = tasksToShow)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState()
    )

    fun selectSmartView(view: SmartView) {
        _activeView.value = view
        _selectedListId.value = null
        clearSelection()
    }

    fun selectList(listId: Long) {
        _selectedListId.value = listId
        clearSelection()
    }

    fun selectDate(dateMillis: Long) {
        _selectedDateMillis.value = dateMillis
        _activeView.value = SmartView.TODAY
        clearSelection()
    }

    fun setPriorityFilter(priority: Priority?) {
        _selectedPriority.value = priority
    }

    fun setSorting(sortBy: TaskSortBy, direction: SortDirection = SortDirection.ASCENDING) {
        _sortBy.value = sortBy
        _sortDirection.value = direction
    }

    // --- Multi-select Operations (F-011) ---

    fun toggleTaskSelection(taskId: Long) {
        val current = _selectedTaskIds.value
        _selectedTaskIds.value = if (current.contains(taskId)) {
            current - taskId
        } else {
            current + taskId
        }
    }

    fun selectAllTasks(tasks: List<Task>) {
        _selectedTaskIds.value = tasks.map { it.id }.toSet()
    }

    fun clearSelection() {
        _selectedTaskIds.value = emptySet()
    }

    fun bulkSetCompleted(isCompleted: Boolean) {
        val ids = _selectedTaskIds.value.toList()
        viewModelScope.launch {
            ids.forEach { taskId ->
                taskRepository.setTaskCompleted(taskId, isCompleted)
            }
            clearSelection()
            _userMessage.value = "${ids.size} tasks updated"
        }
    }

    fun bulkDelete() {
        val ids = _selectedTaskIds.value.toList()
        viewModelScope.launch {
            ids.forEach { taskId ->
                taskRepository.softDeleteTask(taskId)
            }
            clearSelection()
            _userMessage.value = "${ids.size} tasks moved to trash"
        }
    }

    fun bulkSetPriority(priority: Priority) {
        val ids = _selectedTaskIds.value.toList()
        viewModelScope.launch {
            ids.forEach { taskId ->
                val task = taskRepository.getTaskByIdOnce(taskId)
                if (task != null) {
                    taskRepository.updateTask(task.copy(priority = priority))
                }
            }
            clearSelection()
            _userMessage.value = "${ids.size} tasks updated"
        }
    }

    fun bulkMoveToList(listId: Long) {
        val ids = _selectedTaskIds.value.toList()
        viewModelScope.launch {
            ids.forEach { taskId ->
                val task = taskRepository.getTaskByIdOnce(taskId)
                if (task != null) {
                    taskRepository.updateTask(task.copy(listId = listId))
                }
            }
            clearSelection()
            _userMessage.value = "${ids.size} tasks moved"
        }
    }

    // --- Saved Filters Operations (F-028) ---

    fun saveCurrentFilter(name: String) {
        val priorityStr = _selectedPriority.value?.name ?: "NONE"
        val listId = _selectedListId.value
        val queryJson = """{"priority":"$priorityStr","listId":${listId ?: "null"}}"""
        viewModelScope.launch {
            savedFilterRepository.insertFilter(
                SavedFilter(name = name.trim(), queryJson = queryJson)
            )
            _userMessage.value = "Filter '$name' saved"
        }
    }

    fun applySavedFilter(filter: SavedFilter) {
        try {
            val isHigh = filter.queryJson.contains("HIGH")
            val isMedium = filter.queryJson.contains("MEDIUM")
            val isLow = filter.queryJson.contains("LOW")
            val priority = when {
                isHigh -> Priority.HIGH
                isMedium -> Priority.MEDIUM
                isLow -> Priority.LOW
                else -> null
            }
            _selectedPriority.value = priority
            _activeView.value = SmartView.ALL
            _userMessage.value = "Applied filter: ${filter.name}"
        } catch (_: Exception) {}
    }

    fun deleteSavedFilter(filterId: Long) {
        viewModelScope.launch {
            savedFilterRepository.deleteFilter(filterId)
            _userMessage.value = "Filter removed"
        }
    }

    // --- Single Task Operations ---

    fun duplicateTask(taskId: Long) {
        viewModelScope.launch {
            val newId = taskRepository.duplicateTask(taskId)
            if (newId != null) {
                _userMessage.value = "Task duplicated"
            }
        }
    }

    fun reorderTask(taskId: Long, newOrder: Int) {
        viewModelScope.launch {
            taskRepository.updateTaskSortOrder(taskId, newOrder)
        }
    }

    fun quickAddTask(parsedResult: ParsedTaskResult) {
        if (parsedResult.title.isBlank()) return

        viewModelScope.launch {
            // Resolve tags
            val tagIds = mutableListOf<Long>()
            for (tagName in parsedResult.tags) {
                val tag = tagRepository.getOrCreateTag(tagName)
                tagIds.add(tag.id)
            }

            val defaultListId = _selectedListId.value ?: uiState.value.preferences.defaultListId
            val finalPriority = if (parsedResult.priority != Priority.NONE) {
                parsedResult.priority
            } else {
                uiState.value.preferences.defaultPriority
            }

            val task = Task(
                title = parsedResult.title,
                dueDate = parsedResult.dueDate ?: _selectedDateMillis.value,
                dueTime = parsedResult.dueTime,
                isAllDay = parsedResult.isAllDay,
                priority = finalPriority,
                listId = defaultListId,
                recurrenceRule = parsedResult.recurrenceRule
            )

            val newTaskId = taskRepository.insertTask(task, tagIds)

            // If time was specified, schedule a reminder
            if (task.dueDate != null && !task.isAllDay && task.dueTime != null) {
                val trigger = task.dueDate + task.dueTime
                val reminder = Reminder(
                    taskId = newTaskId,
                    triggerAtMillis = trigger,
                    offsetMinutes = 0
                )
                val reminderId = reminderRepository.insertReminder(reminder)
                alarmScheduler.scheduleReminder(
                    reminder.copy(id = reminderId),
                    task.title,
                    task.notes
                )
            }
        }
    }

    fun toggleComplete(task: Task) {
        viewModelScope.launch {
            val newCompleted = !task.isCompleted
            taskRepository.setTaskCompleted(task.id, newCompleted)

            // If completed and has recurrence rule, generate next occurrence
            if (newCompleted && task.recurrenceRule != null) {
                val nextTask = RecurrenceEngine.createNextOccurrence(task)
                if (nextTask != null) {
                    val tagIds = task.tags.map { it.id }
                    val newTaskId = taskRepository.insertTask(nextTask, tagIds)
                    task.reminders.forEach { origReminder ->
                        if (nextTask.dueDate != null) {
                            val newTrigger = nextTask.dueDate + (nextTask.dueTime ?: 0L) - (origReminder.offsetMinutes * 60 * 1000L)
                            val newReminder = Reminder(
                                taskId = newTaskId,
                                triggerAtMillis = newTrigger,
                                offsetMinutes = origReminder.offsetMinutes,
                                type = origReminder.type
                            )
                            val newId = reminderRepository.insertReminder(newReminder)
                            alarmScheduler.scheduleReminder(newReminder.copy(id = newId), nextTask.title, nextTask.notes)
                        }
                    }
                }
            }
        }
    }

    fun toggleStar(task: Task) {
        viewModelScope.launch {
            taskRepository.toggleTaskStarred(task.id)
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            _lastDeletedTask.value = task
            taskRepository.softDeleteTask(task.id)
            _userMessage.value = "Task deleted"
        }
    }

    fun undoDelete() {
        val task = _lastDeletedTask.value ?: return
        viewModelScope.launch {
            taskRepository.restoreTask(task.id)
            _lastDeletedTask.value = null
            _userMessage.value = "Task restored"
        }
    }

    fun clearUserMessage() {
        _userMessage.value = null
    }

    private fun getStartOfTodayMillis(): Long {
        return Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    companion object {
        fun provideFactory(appContainer: AppContainer): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return HomeViewModel(appContainer) as T
                }
            }
    }
}
