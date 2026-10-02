package org.lortodo.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import org.lortodo.data.local.Converters
import org.lortodo.data.local.dao.ReminderDao
import org.lortodo.data.local.dao.SubtaskDao
import org.lortodo.data.local.dao.TagDao
import org.lortodo.data.local.dao.TaskDao
import org.lortodo.data.local.dao.TaskListDao
import org.lortodo.data.local.entity.ReminderEntity
import org.lortodo.data.local.entity.SubtaskEntity
import org.lortodo.data.local.entity.TaskEntity
import org.lortodo.domain.model.Priority
import org.lortodo.domain.model.Reminder
import org.lortodo.domain.model.ReminderType
import org.lortodo.domain.model.RepeatFrom
import org.lortodo.domain.model.Subtask
import org.lortodo.domain.model.Tag
import org.lortodo.domain.model.Task
import org.lortodo.domain.repository.SmartView
import org.lortodo.domain.repository.SortDirection
import org.lortodo.domain.repository.TaskFilter
import org.lortodo.domain.repository.TaskRepository
import org.lortodo.domain.repository.TaskSortBy
import java.util.Calendar

class TaskRepositoryImpl(
    private val taskDao: TaskDao,
    private val subtaskDao: SubtaskDao,
    private val taskListDao: TaskListDao,
    private val tagDao: TagDao,
    private val reminderDao: ReminderDao
) : TaskRepository {

    private val converters = Converters()

    override fun getTasksBySmartView(
        view: SmartView,
        filter: TaskFilter,
        sortBy: TaskSortBy,
        sortDirection: SortDirection
    ): Flow<List<Task>> {
        val now = System.currentTimeMillis()
        val calendar = Calendar.getInstance().apply {
            timeInMillis = now
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startToday = calendar.timeInMillis
        val endToday = startToday + 24 * 60 * 60 * 1000L - 1L

        calendar.add(Calendar.DAY_OF_YEAR, 1)
        val startTomorrow = calendar.timeInMillis
        val endTomorrow = startTomorrow + 24 * 60 * 60 * 1000L - 1L

        calendar.add(Calendar.DAY_OF_YEAR, 6) // 7 days from today
        val endUpcoming = calendar.timeInMillis + 24 * 60 * 60 * 1000L - 1L

        val entitiesFlow = when (view) {
            SmartView.INBOX -> taskDao.getTasksByList(1L)
            SmartView.TODAY -> taskDao.getTodayTasks(endToday)
            SmartView.TOMORROW -> taskDao.getTomorrowTasks(startTomorrow, endTomorrow)
            SmartView.UPCOMING -> taskDao.getUpcomingTasks(startToday, endUpcoming)
            SmartView.OVERDUE -> taskDao.getOverdueTasks(startToday)
            SmartView.ALL -> taskDao.getAllActiveTasks()
            SmartView.COMPLETED -> taskDao.getCompletedTasks()
            SmartView.STARRED -> taskDao.getStarredTasks()
            SmartView.NO_DATE -> taskDao.getNoDateTasks()
            SmartView.TRASH -> taskDao.getTrashTasks()
            SmartView.ARCHIVE -> taskDao.getArchivedTasks()
        }

        return entitiesFlow.map { entities ->
            var domainTasks = entities.map { entityToDomain(it) }

            // Apply in-memory filters
            if (filter.priority != null) {
                domainTasks = domainTasks.filter { it.priority == filter.priority }
            }
            if (filter.listId != null) {
                domainTasks = domainTasks.filter { it.listId == filter.listId }
            }
            if (filter.isCompleted != null) {
                domainTasks = domainTasks.filter { it.isCompleted == filter.isCompleted }
            }

            // Apply sorting
            val sorted = when (sortBy) {
                TaskSortBy.MANUAL -> domainTasks.sortedBy { it.sortOrder }
                TaskSortBy.DUE_DATE -> domainTasks.sortedWith(
                    compareBy(nullsLast()) { it.dueDate }
                )
                TaskSortBy.PRIORITY -> domainTasks.sortedByDescending { it.priority.level }
                TaskSortBy.CREATED_DATE -> domainTasks.sortedByDescending { it.createdAt }
                TaskSortBy.TITLE -> domainTasks.sortedBy { it.title.lowercase() }
            }

            if (sortDirection == SortDirection.DESCENDING) {
                sorted.reversed()
            } else {
                sorted
            }
        }
    }

    override fun getTasksByList(listId: Long): Flow<List<Task>> {
        return taskDao.getTasksByList(listId).map { list ->
            list.map { entityToDomain(it) }
        }
    }

    override fun getTaskById(taskId: Long): Flow<Task?> {
        return combine(
            taskDao.getTaskById(taskId),
            subtaskDao.getSubtasksForTask(taskId),
            tagDao.getTagsForTask(taskId),
            reminderDao.getRemindersForTask(taskId)
        ) { taskEntity, subtaskEntities, tagEntities, reminderEntities ->
            taskEntity?.let { entity ->
                entityToDomain(entity).copy(
                    subtasks = subtaskEntities.map { subtaskEntityToDomain(it) },
                    tags = tagEntities.map { Tag(it.id, it.name, it.colorHex) },
                    reminders = reminderEntities.map { reminderEntityToDomain(it) }
                )
            }
        }
    }

    override suspend fun getTaskByIdOnce(taskId: Long): Task? {
        val entity = taskDao.getTaskByIdOnce(taskId) ?: return null
        val subtasks = subtaskDao.getSubtasksForTaskOnce(taskId).map { subtaskEntityToDomain(it) }
        val tags = tagDao.getTagsForTaskOnce(taskId).map { Tag(it.id, it.name, it.colorHex) }
        val reminders = reminderDao.getRemindersForTaskOnce(taskId).map { reminderEntityToDomain(it) }
        return entityToDomain(entity).copy(subtasks = subtasks, tags = tags, reminders = reminders)
    }

    override fun searchTasks(query: String): Flow<List<Task>> {
        val trimmed = query.trim()
        val flow = if (trimmed.isBlank()) {
            taskDao.getAllActiveTasks()
        } else {
            val ftsQuery = if (trimmed.contains("*") || trimmed.contains("\"")) trimmed else "$trimmed*"
            try {
                taskDao.searchTasks(ftsQuery)
            } catch (_: Exception) {
                taskDao.searchTasksLike(trimmed)
            }
        }
        return flow.map { list -> list.map { entityToDomain(it) } }
    }

    override fun getTasksForDate(dateMillis: Long): Flow<List<Task>> {
        val cal = Calendar.getInstance().apply {
            timeInMillis = dateMillis
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val start = cal.timeInMillis
        val end = start + 24 * 60 * 60 * 1000L - 1L
        return taskDao.getTasksForDate(start, end).map { list ->
            list.map { entityToDomain(it) }
        }
    }

    override fun getTasksForDateRange(startDateMillis: Long, endDateMillis: Long): Flow<List<Task>> {
        return taskDao.getTasksForDateRange(startDateMillis, endDateMillis).map { list ->
            list.map { entityToDomain(it) }
        }
    }

    override suspend fun insertTask(task: Task, tagIds: List<Long>): Long {
        val entity = domainToEntity(task)
        val newId = taskDao.insertTask(entity)
        if (tagIds.isNotEmpty()) {
            taskDao.setTagsForTask(newId, tagIds)
        }
        if (task.subtasks.isNotEmpty()) {
            subtaskDao.insertSubtasks(task.subtasks.map { domainToSubtaskEntity(it.copy(taskId = newId)) })
        }
        return newId
    }

    override suspend fun updateTask(task: Task, tagIds: List<Long>?) {
        taskDao.updateTask(domainToEntity(task))
        if (tagIds != null) {
            taskDao.setTagsForTask(task.id, tagIds)
        }
    }

    override suspend fun setTaskCompleted(taskId: Long, isCompleted: Boolean) {
        val completedAt = if (isCompleted) System.currentTimeMillis() else null
        taskDao.setTaskCompleted(taskId, isCompleted, completedAt)
    }

    override suspend fun toggleTaskStarred(taskId: Long) {
        taskDao.toggleTaskStarred(taskId)
    }

    override suspend fun softDeleteTask(taskId: Long) {
        taskDao.softDeleteTask(taskId)
    }

    override suspend fun restoreTask(taskId: Long) {
        taskDao.restoreTask(taskId)
    }

    override suspend fun permanentlyDeleteTask(taskId: Long) {
        taskDao.permanentlyDeleteTask(taskId)
    }

    override suspend fun emptyTrash() {
        taskDao.emptyTrash()
    }

    override suspend fun purgeDeletedTasksOlderThan(thresholdMillis: Long) {
        taskDao.purgeDeletedTasksOlderThan(thresholdMillis)
    }

    override suspend fun archiveTask(taskId: Long) {
        taskDao.archiveTask(taskId)
    }

    override suspend fun unarchiveTask(taskId: Long) {
        taskDao.unarchiveTask(taskId)
    }

    override suspend fun duplicateTask(taskId: Long): Long? {
        val original = getTaskByIdOnce(taskId) ?: return null
        val duplicated = original.copy(
            id = 0,
            title = "${original.title} (Copy)",
            isCompleted = false,
            completedAt = null,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        val tagIds = original.tags.map { it.id }
        return insertTask(duplicated, tagIds)
    }

    override suspend fun updateTaskSortOrder(taskId: Long, newOrder: Int) {
        taskDao.updateTaskSortOrder(taskId, newOrder)
    }

    override fun getSubtasks(taskId: Long): Flow<List<Subtask>> {
        return subtaskDao.getSubtasksForTask(taskId).map { list ->
            list.map { subtaskEntityToDomain(it) }
        }
    }

    override suspend fun insertSubtask(subtask: Subtask): Long {
        return subtaskDao.insertSubtask(domainToSubtaskEntity(subtask))
    }

    override suspend fun updateSubtask(subtask: Subtask) {
        subtaskDao.updateSubtask(domainToSubtaskEntity(subtask))
    }

    override suspend fun deleteSubtask(subtaskId: Long) {
        subtaskDao.deleteSubtask(subtaskId)
    }

    override suspend fun toggleSubtask(subtaskId: Long) {
        subtaskDao.toggleSubtask(subtaskId)
    }

    override fun getTagsForTask(taskId: Long): Flow<List<Tag>> {
        return tagDao.getTagsForTask(taskId).map { list ->
            list.map { Tag(it.id, it.name, it.colorHex) }
        }
    }

    override suspend fun setTagsForTask(taskId: Long, tagIds: List<Long>) {
        taskDao.setTagsForTask(taskId, tagIds)
    }

    // Mapping helpers
    private fun entityToDomain(entity: TaskEntity): Task {
        return Task(
            id = entity.id,
            title = entity.title,
            notes = entity.notes,
            listId = entity.listId,
            priority = Priority.fromLevel(entity.priority),
            isCompleted = entity.isCompleted,
            completedAt = entity.completedAt,
            dueDate = entity.dueDate,
            dueTime = entity.dueTime,
            isAllDay = entity.isAllDay,
            isStarred = entity.isStarred,
            recurrenceRule = converters.toRecurrenceRule(entity.recurrenceRuleJson),
            repeatFrom = if (entity.repeatFrom == "COMPLETION_DATE") RepeatFrom.COMPLETION_DATE else RepeatFrom.DUE_DATE,
            estimateMinutes = entity.estimateMinutes,
            sortOrder = entity.sortOrder,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt,
            deletedAt = entity.deletedAt,
            archivedAt = entity.archivedAt
        )
    }

    private fun domainToEntity(domain: Task): TaskEntity {
        return TaskEntity(
            id = domain.id,
            title = domain.title,
            notes = domain.notes,
            listId = domain.listId,
            priority = domain.priority.level,
            isCompleted = domain.isCompleted,
            completedAt = domain.completedAt,
            dueDate = domain.dueDate,
            dueTime = domain.dueTime,
            isAllDay = domain.isAllDay,
            isStarred = domain.isStarred,
            recurrenceRuleJson = converters.fromRecurrenceRule(domain.recurrenceRule),
            repeatFrom = if (domain.repeatFrom == RepeatFrom.COMPLETION_DATE) "COMPLETION_DATE" else "DUE_DATE",
            estimateMinutes = domain.estimateMinutes,
            sortOrder = domain.sortOrder,
            createdAt = domain.createdAt,
            updatedAt = domain.updatedAt,
            deletedAt = domain.deletedAt,
            archivedAt = domain.archivedAt
        )
    }

    private fun subtaskEntityToDomain(entity: SubtaskEntity): Subtask {
        return Subtask(
            id = entity.id,
            taskId = entity.taskId,
            title = entity.title,
            isDone = entity.isDone,
            sortOrder = entity.sortOrder
        )
    }

    private fun domainToSubtaskEntity(domain: Subtask): SubtaskEntity {
        return SubtaskEntity(
            id = domain.id,
            taskId = domain.taskId,
            title = domain.title,
            isDone = domain.isDone,
            sortOrder = domain.sortOrder
        )
    }

    private fun reminderEntityToDomain(entity: ReminderEntity): Reminder {
        return Reminder(
            id = entity.id,
            taskId = entity.taskId,
            triggerAtMillis = entity.triggerAtMillis,
            offsetMinutes = entity.offsetMinutes,
            type = try { ReminderType.valueOf(entity.type) } catch (_: Exception) { ReminderType.NOTIFICATION },
            isFired = entity.isFired
        )
    }
}
