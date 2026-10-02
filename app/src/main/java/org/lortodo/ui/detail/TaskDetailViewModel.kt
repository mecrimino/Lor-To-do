package org.lortodo.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.lortodo.core.AppContainer
import org.lortodo.domain.model.Priority
import org.lortodo.domain.model.RecurrenceRule
import org.lortodo.domain.model.Reminder
import org.lortodo.domain.model.Subtask
import org.lortodo.domain.model.Tag
import org.lortodo.domain.model.Task
import org.lortodo.domain.model.TaskList

data class TaskDetailUiState(
    val taskId: Long = 0L,
    val title: String = "",
    val notes: String = "",
    val listId: Long = 1L,
    val priority: Priority = Priority.NONE,
    val isCompleted: Boolean = false,
    val isStarred: Boolean = false,
    val dueDate: Long? = null,
    val dueTime: Long? = null,
    val isAllDay: Boolean = true,
    val recurrenceRule: RecurrenceRule? = null,
    val estimateMinutes: Int? = null,
    val subtasks: List<Subtask> = emptyList(),
    val tags: List<Tag> = emptyList(),
    val availableTags: List<Tag> = emptyList(),
    val availableLists: List<TaskList> = emptyList(),
    val reminders: List<Reminder> = emptyList(),
    val isSaved: Boolean = false
)

class TaskDetailViewModel(
    private val appContainer: AppContainer,
    private val initialTaskId: Long
) : ViewModel() {

    private val taskRepository = appContainer.taskRepository
    private val taskListRepository = appContainer.taskListRepository
    private val tagRepository = appContainer.tagRepository
    private val reminderRepository = appContainer.reminderRepository
    private val alarmScheduler = appContainer.alarmScheduler

    private val _uiState = MutableStateFlow(TaskDetailUiState(taskId = initialTaskId))
    val uiState: StateFlow<TaskDetailUiState> = _uiState

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            val lists = taskListRepository.getAllLists().first()
            val allTags = tagRepository.getAllTags().first()

            if (initialTaskId != 0L) {
                val task = taskRepository.getTaskByIdOnce(initialTaskId)
                if (task != null) {
                    val reminders = reminderRepository.getRemindersForTask(initialTaskId).first()
                    _uiState.value = TaskDetailUiState(
                        taskId = task.id,
                        title = task.title,
                        notes = task.notes,
                        listId = task.listId,
                        priority = task.priority,
                        isCompleted = task.isCompleted,
                        isStarred = task.isStarred,
                        dueDate = task.dueDate,
                        dueTime = task.dueTime,
                        isAllDay = task.isAllDay,
                        recurrenceRule = task.recurrenceRule,
                        estimateMinutes = task.estimateMinutes,
                        subtasks = task.subtasks,
                        tags = task.tags,
                        availableTags = allTags,
                        availableLists = lists,
                        reminders = reminders
                    )
                    return@launch
                }
            }

            _uiState.value = _uiState.value.copy(
                availableLists = lists,
                availableTags = allTags
            )
        }
    }

    fun updateTitle(title: String) {
        _uiState.value = _uiState.value.copy(title = title)
    }

    fun updateNotes(notes: String) {
        _uiState.value = _uiState.value.copy(notes = notes)
    }

    fun updateListId(listId: Long) {
        _uiState.value = _uiState.value.copy(listId = listId)
    }

    fun updatePriority(priority: Priority) {
        _uiState.value = _uiState.value.copy(priority = priority)
    }

    fun toggleStarred() {
        _uiState.value = _uiState.value.copy(isStarred = !_uiState.value.isStarred)
    }

    fun updateDueDate(dateMillis: Long?) {
        _uiState.value = _uiState.value.copy(dueDate = dateMillis)
    }

    fun updateDueTime(timeMillis: Long?, isAllDay: Boolean) {
        _uiState.value = _uiState.value.copy(dueTime = timeMillis, isAllDay = isAllDay)
    }

    fun updateRecurrenceRule(rule: RecurrenceRule?) {
        _uiState.value = _uiState.value.copy(recurrenceRule = rule)
    }

    fun updateEstimateMinutes(mins: Int?) {
        _uiState.value = _uiState.value.copy(estimateMinutes = mins)
    }

    fun addSubtask(title: String) {
        val current = _uiState.value.subtasks
        val newSubtask = Subtask(
            id = 0L,
            taskId = _uiState.value.taskId,
            title = title,
            sortOrder = current.size
        )
        _uiState.value = _uiState.value.copy(subtasks = current + newSubtask)
    }

    fun toggleSubtask(subtask: Subtask) {
        val updated = _uiState.value.subtasks.map {
            if (it == subtask) it.copy(isDone = !it.isDone) else it
        }
        _uiState.value = _uiState.value.copy(subtasks = updated)
    }

    fun deleteSubtask(subtask: Subtask) {
        _uiState.value = _uiState.value.copy(
            subtasks = _uiState.value.subtasks.filter { it != subtask }
        )
    }

    fun toggleTag(tag: Tag) {
        val current = _uiState.value.tags
        val updated = if (current.any { it.id == tag.id }) {
            current.filter { it.id != tag.id }
        } else {
            current + tag
        }
        _uiState.value = _uiState.value.copy(tags = updated)
    }

    fun addNewTag(name: String) {
        viewModelScope.launch {
            val tag = tagRepository.getOrCreateTag(name)
            val updatedAvailable = (_uiState.value.availableTags + tag).distinctBy { it.id }
            val updatedSelected = (_uiState.value.tags + tag).distinctBy { it.id }
            _uiState.value = _uiState.value.copy(
                availableTags = updatedAvailable,
                tags = updatedSelected
            )
        }
    }

    fun addReminderOffset(offsetMinutes: Int) {
        val dueDate = _uiState.value.dueDate ?: return
        val dueTime = _uiState.value.dueTime ?: (12 * 3600 * 1000L)
        val triggerTime = dueDate + dueTime - (offsetMinutes * 60 * 1000L)

        val newReminder = Reminder(
            taskId = _uiState.value.taskId,
            triggerAtMillis = triggerTime,
            offsetMinutes = offsetMinutes
        )
        _uiState.value = _uiState.value.copy(
            reminders = _uiState.value.reminders + newReminder
        )
    }

    fun removeReminder(reminder: Reminder) {
        _uiState.value = _uiState.value.copy(
            reminders = _uiState.value.reminders.filter { it != reminder }
        )
    }

    fun saveTask(onComplete: () -> Unit) {
        val state = _uiState.value
        if (state.title.isBlank()) return

        viewModelScope.launch {
            val task = Task(
                id = state.taskId,
                title = state.title.trim(),
                notes = state.notes.trim(),
                listId = state.listId,
                priority = state.priority,
                isCompleted = state.isCompleted,
                isStarred = state.isStarred,
                dueDate = state.dueDate,
                dueTime = state.dueTime,
                isAllDay = state.isAllDay,
                recurrenceRule = state.recurrenceRule,
                estimateMinutes = state.estimateMinutes,
                subtasks = state.subtasks
            )

            val tagIds = state.tags.map { it.id }
            val savedId = if (state.taskId == 0L) {
                taskRepository.insertTask(task, tagIds)
            } else {
                taskRepository.updateTask(task, tagIds)
                // Update subtasks
                state.subtasks.forEach { subtask ->
                    if (subtask.id == 0L) {
                        taskRepository.insertSubtask(subtask.copy(taskId = state.taskId))
                    } else {
                        taskRepository.updateSubtask(subtask)
                    }
                }
                state.taskId
            }

            // Sync Reminders
            reminderRepository.deleteRemindersForTask(savedId)
            state.reminders.forEach { r ->
                val newR = r.copy(taskId = savedId)
                val newRId = reminderRepository.insertReminder(newR)
                alarmScheduler.scheduleReminder(newR.copy(id = newRId), task.title, task.notes)
            }

            _uiState.value = _uiState.value.copy(isSaved = true)
            onComplete()
        }
    }

    fun deleteTask(onComplete: () -> Unit) {
        if (_uiState.value.taskId != 0L) {
            viewModelScope.launch {
                taskRepository.softDeleteTask(_uiState.value.taskId)
                onComplete()
            }
        } else {
            onComplete()
        }
    }

    fun duplicateTask(onComplete: (Long) -> Unit) {
        if (_uiState.value.taskId != 0L) {
            viewModelScope.launch {
                val newId = taskRepository.duplicateTask(_uiState.value.taskId)
                if (newId != null) {
                    onComplete(newId)
                }
            }
        }
    }

    fun saveAsTemplate(name: String, onComplete: () -> Unit) {
        val state = _uiState.value
        if (name.isBlank() || state.title.isBlank()) return

        viewModelScope.launch {
            val subtasksJson = state.subtasks.joinToString(separator = ",", prefix = "[", postfix = "]") { s ->
                """{"title":"${s.title.replace("\"", "\\\"")}","isDone":false,"sortOrder":${s.sortOrder}}"""
            }
            val payload = """{"title":"${state.title.replace("\"", "\\\"")}","notes":"${state.notes.replace("\"", "\\\"")}","priority":"${state.priority.name}","estimateMinutes":${state.estimateMinutes ?: "null"},"subtasks":$subtasksJson}"""
            appContainer.templateRepository.insertTemplate(
                org.lortodo.domain.model.TaskTemplate(
                    name = name.trim(),
                    payloadJson = payload
                )
            )
            onComplete()
        }
    }

    companion object {
        fun provideFactory(appContainer: AppContainer, taskId: Long): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return TaskDetailViewModel(appContainer, taskId) as T
                }
            }
    }
}
