package org.lortodo.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.lortodo.core.AppContainer
import org.lortodo.data.backup.BackupManager
import org.lortodo.domain.model.AccentColor
import org.lortodo.domain.model.AppTheme
import org.lortodo.domain.model.FirstDayOfWeek
import org.lortodo.domain.model.UserPreferences
import org.lortodo.domain.model.ViewDensity
import org.lortodo.domain.repository.SmartView

data class SettingsUiState(
    val preferences: UserPreferences = UserPreferences(),
    val totalTaskCount: Int = 0,
    val message: String? = null
)

class SettingsViewModel(
    private val appContainer: AppContainer
) : ViewModel() {

    private val userPreferencesRepository = appContainer.userPreferencesRepository
    private val taskRepository = appContainer.taskRepository
    private val taskListRepository = appContainer.taskListRepository
    private val tagRepository = appContainer.tagRepository

    private val _message = MutableStateFlow<String?>(null)

    val uiState: StateFlow<SettingsUiState> = combine(
        userPreferencesRepository.userPreferences,
        taskRepository.getTasksBySmartView(SmartView.ALL),
        _message
    ) { prefs, allTasks, msg ->
        SettingsUiState(
            preferences = prefs,
            totalTaskCount = allTasks.size,
            message = msg
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettingsUiState()
    )

    fun updateUserName(name: String) {
        viewModelScope.launch { userPreferencesRepository.updateUserName(name) }
    }

    fun updateTheme(theme: AppTheme) {
        viewModelScope.launch { userPreferencesRepository.updateTheme(theme) }
    }

    fun updateAccentColor(accentColor: AccentColor) {
        viewModelScope.launch { userPreferencesRepository.updateAccentColor(accentColor) }
    }

    fun updateDensity(density: ViewDensity) {
        viewModelScope.launch { userPreferencesRepository.updateDensity(density) }
    }

    fun updateFirstDayOfWeek(firstDay: FirstDayOfWeek) {
        viewModelScope.launch { userPreferencesRepository.updateFirstDayOfWeek(firstDay) }
    }

    fun toggleAppLock(enabled: Boolean) {
        viewModelScope.launch { userPreferencesRepository.setAppLockEnabled(enabled) }
    }

    fun toggleHideInRecents(enabled: Boolean) {
        viewModelScope.launch { userPreferencesRepository.setHideInRecents(enabled) }
    }

    fun toggleHideSensitiveNotifications(enabled: Boolean) {
        viewModelScope.launch { userPreferencesRepository.setHideSensitiveNotifications(enabled) }
    }

    fun toggleDailySummary(enabled: Boolean) {
        viewModelScope.launch {
            val current = uiState.value.preferences.dailySummaryTimeMillis
            userPreferencesRepository.setDailySummary(enabled, current)
        }
    }

    fun toggleOverdueNudges(enabled: Boolean) {
        viewModelScope.launch { userPreferencesRepository.setOverdueNudges(enabled) }
    }

    fun toggleHaptics(enabled: Boolean) {
        viewModelScope.launch { userPreferencesRepository.setHapticsEnabled(enabled) }
    }

    fun toggleSound(enabled: Boolean) {
        viewModelScope.launch { userPreferencesRepository.setSoundEnabled(enabled) }
    }

    suspend fun exportJsonData(): String {
        val tasks = taskRepository.getTasksBySmartView(SmartView.ALL).first()
        val lists = taskListRepository.getAllLists().first()
        val tags = tagRepository.getAllTags().first()
        val prefs = userPreferencesRepository.userPreferences.first()
        return BackupManager.exportToJson(tasks, lists, tags, prefs)
    }

    suspend fun exportCsvData(): String {
        val tasks = taskRepository.getTasksBySmartView(SmartView.ALL).first()
        return BackupManager.exportToCsv(tasks)
    }

    suspend fun exportMarkdownData(): String {
        val tasks = taskRepository.getTasksBySmartView(SmartView.ALL).first()
        return BackupManager.exportToMarkdown(tasks)
    }

    fun importJsonData(jsonString: String, replaceExisting: Boolean) {
        viewModelScope.launch {
            val payload = BackupManager.parseBackupJson(jsonString)
            if (payload == null) {
                _message.value = "Failed to parse backup JSON"
                return@launch
            }

            if (replaceExisting) {
                taskRepository.emptyTrash()
            }

            // Import Lists
            payload.lists.forEach { bl ->
                taskListRepository.insertList(
                    org.lortodo.domain.model.TaskList(
                        id = if (replaceExisting) bl.id else 0L,
                        name = bl.name,
                        colorHex = bl.colorHex,
                        icon = bl.icon
                    )
                )
            }

            // Import Tags
            payload.tags.forEach { bt ->
                tagRepository.getOrCreateTag(bt.name, bt.colorHex)
            }

            // Import Tasks
            payload.tasks.forEach { bt ->
                val task = org.lortodo.domain.model.Task(
                    id = if (replaceExisting) bt.id else 0L,
                    title = bt.title,
                    notes = bt.notes,
                    listId = bt.listId,
                    priority = bt.priority,
                    isCompleted = bt.isCompleted,
                    completedAt = bt.completedAt,
                    dueDate = bt.dueDate,
                    dueTime = bt.dueTime,
                    isAllDay = bt.isAllDay,
                    isStarred = bt.isStarred,
                    recurrenceRule = bt.recurrenceRule,
                    repeatFrom = bt.repeatFrom,
                    estimateMinutes = bt.estimateMinutes,
                    sortOrder = bt.sortOrder,
                    createdAt = bt.createdAt,
                    updatedAt = bt.updatedAt,
                    subtasks = bt.subtasks.map { s ->
                        org.lortodo.domain.model.Subtask(
                            taskId = if (replaceExisting) bt.id else 0L,
                            title = s.title,
                            isDone = s.isDone,
                            sortOrder = s.sortOrder
                        )
                    }
                )
                taskRepository.insertTask(task)
            }

            _message.value = "Restored ${payload.tasks.size} tasks successfully!"
        }
    }

    fun importCsvData(csvContent: String) {
        viewModelScope.launch {
            val imported = BackupManager.importFromCsv(csvContent)
            imported.forEach { task ->
                taskRepository.insertTask(task)
            }
            _message.value = "Imported ${imported.size} tasks from CSV!"
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            taskRepository.emptyTrash()
            val allTasks = taskRepository.getTasksBySmartView(SmartView.ALL).first()
            allTasks.forEach { task ->
                taskRepository.permanentlyDeleteTask(task.id)
            }
            _message.value = "All data cleared successfully."
        }
    }

    fun clearMessage() {
        _message.value = null
    }

    companion object {
        fun provideFactory(appContainer: AppContainer): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return SettingsViewModel(appContainer) as T
                }
            }
    }
}
