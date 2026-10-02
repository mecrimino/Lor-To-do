package org.lortodo.domain.repository

import kotlinx.coroutines.flow.Flow
import org.lortodo.domain.model.Priority
import org.lortodo.domain.model.Subtask
import org.lortodo.domain.model.Tag
import org.lortodo.domain.model.Task

enum class SmartView {
    INBOX,
    TODAY,
    TOMORROW,
    UPCOMING,
    OVERDUE,
    ALL,
    COMPLETED,
    STARRED,
    NO_DATE,
    TRASH,
    ARCHIVE
}

enum class TaskSortBy {
    MANUAL,
    DUE_DATE,
    PRIORITY,
    CREATED_DATE,
    TITLE
}

enum class SortDirection {
    ASCENDING,
    DESCENDING
}

data class TaskFilter(
    val listId: Long? = null,
    val tagIds: List<Long> = emptyList(),
    val priority: Priority? = null,
    val hasReminder: Boolean? = null,
    val hasSubtasks: Boolean? = null,
    val isCompleted: Boolean? = null
)

interface TaskRepository {
    fun getTasksBySmartView(
        view: SmartView,
        filter: TaskFilter = TaskFilter(),
        sortBy: TaskSortBy = TaskSortBy.MANUAL,
        sortDirection: SortDirection = SortDirection.ASCENDING
    ): Flow<List<Task>>

    fun getTasksByList(listId: Long): Flow<List<Task>>

    fun getTaskById(taskId: Long): Flow<Task?>

    suspend fun getTaskByIdOnce(taskId: Long): Task?

    fun searchTasks(query: String): Flow<List<Task>>

    fun getTasksForDate(dateMillis: Long): Flow<List<Task>>

    fun getTasksForDateRange(startDateMillis: Long, endDateMillis: Long): Flow<List<Task>>

    suspend fun insertTask(task: Task, tagIds: List<Long> = emptyList()): Long

    suspend fun updateTask(task: Task, tagIds: List<Long>? = null)

    suspend fun setTaskCompleted(taskId: Long, isCompleted: Boolean)

    suspend fun toggleTaskStarred(taskId: Long)

    suspend fun softDeleteTask(taskId: Long)

    suspend fun restoreTask(taskId: Long)

    suspend fun permanentlyDeleteTask(taskId: Long)

    suspend fun emptyTrash()

    suspend fun purgeDeletedTasksOlderThan(thresholdMillis: Long)

    suspend fun archiveTask(taskId: Long)

    suspend fun unarchiveTask(taskId: Long)

    suspend fun duplicateTask(taskId: Long): Long?

    suspend fun updateTaskSortOrder(taskId: Long, newOrder: Int)

    // Subtasks
    fun getSubtasks(taskId: Long): Flow<List<Subtask>>
    suspend fun insertSubtask(subtask: Subtask): Long
    suspend fun updateSubtask(subtask: Subtask)
    suspend fun deleteSubtask(subtaskId: Long)
    suspend fun toggleSubtask(subtaskId: Long)

    // Tags association
    fun getTagsForTask(taskId: Long): Flow<List<Tag>>
    suspend fun setTagsForTask(taskId: Long, tagIds: List<Long>)
}
