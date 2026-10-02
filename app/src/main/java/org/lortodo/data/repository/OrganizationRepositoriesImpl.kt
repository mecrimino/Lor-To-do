package org.lortodo.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import org.lortodo.data.local.dao.FocusSessionDao
import org.lortodo.data.local.dao.ReminderDao
import org.lortodo.data.local.dao.SavedFilterDao
import org.lortodo.data.local.dao.TagDao
import org.lortodo.data.local.dao.TaskDao
import org.lortodo.data.local.dao.TaskListDao
import org.lortodo.data.local.dao.TemplateDao
import org.lortodo.data.local.entity.FocusSessionEntity
import org.lortodo.data.local.entity.ReminderEntity
import org.lortodo.data.local.entity.SavedFilterEntity
import org.lortodo.data.local.entity.TagEntity
import org.lortodo.data.local.entity.TaskListEntity
import org.lortodo.data.local.entity.TemplateEntity
import org.lortodo.domain.model.FocusSession
import org.lortodo.domain.model.Reminder
import org.lortodo.domain.model.ReminderType
import org.lortodo.domain.model.SavedFilter
import org.lortodo.domain.model.Tag
import org.lortodo.domain.model.TaskList
import org.lortodo.domain.model.TaskTemplate
import org.lortodo.domain.repository.FocusRepository
import org.lortodo.domain.repository.ReminderRepository
import org.lortodo.domain.repository.SavedFilterRepository
import org.lortodo.domain.repository.TagRepository
import org.lortodo.domain.repository.TaskListRepository
import org.lortodo.domain.repository.TemplateRepository
import java.util.Calendar

class TaskListRepositoryImpl(
    private val taskListDao: TaskListDao,
    private val taskDao: TaskDao
) : TaskListRepository {

    override fun getAllLists(): Flow<List<TaskList>> {
        return combine(taskListDao.getAllActiveLists(), taskDao.getAllActiveTasks()) { lists, tasks ->
            lists.map { entity ->
                val count = tasks.count { it.listId == entity.id }
                TaskList(
                    id = entity.id,
                    name = entity.name,
                    colorHex = entity.colorHex,
                    icon = entity.icon,
                    folderId = entity.folderId,
                    sortOrder = entity.sortOrder,
                    isArchived = entity.isArchived,
                    taskCount = count
                )
            }
        }
    }

    override fun getListById(id: Long): Flow<TaskList?> {
        return taskListDao.getListById(id).map { entity ->
            entity?.let {
                TaskList(
                    id = it.id,
                    name = it.name,
                    colorHex = it.colorHex,
                    icon = it.icon,
                    folderId = it.folderId,
                    sortOrder = it.sortOrder,
                    isArchived = it.isArchived
                )
            }
        }
    }

    override suspend fun insertList(list: TaskList): Long {
        return taskListDao.insertList(
            TaskListEntity(
                name = list.name,
                colorHex = list.colorHex,
                icon = list.icon,
                folderId = list.folderId,
                sortOrder = list.sortOrder,
                isArchived = list.isArchived
            )
        )
    }

    override suspend fun updateList(list: TaskList) {
        taskListDao.updateList(
            TaskListEntity(
                id = list.id,
                name = list.name,
                colorHex = list.colorHex,
                icon = list.icon,
                folderId = list.folderId,
                sortOrder = list.sortOrder,
                isArchived = list.isArchived
            )
        )
    }

    override suspend fun deleteList(id: Long) {
        if (id != 1L) { // Do not delete default Inbox
            taskListDao.deleteList(id)
        }
    }

    override suspend fun archiveList(id: Long, isArchived: Boolean) {
        taskListDao.archiveList(id, isArchived)
    }
}

class TagRepositoryImpl(
    private val tagDao: TagDao
) : TagRepository {

    override fun getAllTags(): Flow<List<Tag>> {
        return tagDao.getAllTags().map { list ->
            list.map { Tag(it.id, it.name, it.colorHex) }
        }
    }

    override fun getTagById(id: Long): Flow<Tag?> {
        return tagDao.getTagById(id).map { it?.let { Tag(it.id, it.name, it.colorHex) } }
    }

    override suspend fun insertTag(tag: Tag): Long {
        return tagDao.insertTag(TagEntity(id = tag.id, name = tag.name, colorHex = tag.colorHex))
    }

    override suspend fun updateTag(tag: Tag) {
        tagDao.updateTag(TagEntity(id = tag.id, name = tag.name, colorHex = tag.colorHex))
    }

    override suspend fun deleteTag(id: Long) {
        tagDao.deleteTag(id)
    }

    override suspend fun getOrCreateTag(name: String, colorHex: String): Tag {
        val existing = tagDao.getTagByName(name)
        if (existing != null) {
            return Tag(existing.id, existing.name, existing.colorHex)
        }
        val id = tagDao.insertTag(TagEntity(name = name, colorHex = colorHex))
        return Tag(id, name, colorHex)
    }
}

class ReminderRepositoryImpl(
    private val reminderDao: ReminderDao
) : ReminderRepository {

    override fun getRemindersForTask(taskId: Long): Flow<List<Reminder>> {
        return reminderDao.getRemindersForTask(taskId).map { list ->
            list.map { entityToDomain(it) }
        }
    }

    override fun getAllActiveReminders(): Flow<List<Reminder>> {
        return reminderDao.getAllActiveReminders().map { list ->
            list.map { entityToDomain(it) }
        }
    }

    override suspend fun insertReminder(reminder: Reminder): Long {
        return reminderDao.insertReminder(domainToEntity(reminder))
    }

    override suspend fun updateReminder(reminder: Reminder) {
        reminderDao.updateReminder(domainToEntity(reminder))
    }

    override suspend fun deleteReminder(id: Long) {
        reminderDao.deleteReminder(id)
    }

    override suspend fun deleteRemindersForTask(taskId: Long) {
        reminderDao.deleteRemindersForTask(taskId)
    }

    override suspend fun markReminderFired(id: Long) {
        reminderDao.markReminderFired(id)
    }

    private fun entityToDomain(entity: ReminderEntity): Reminder {
        return Reminder(
            id = entity.id,
            taskId = entity.taskId,
            triggerAtMillis = entity.triggerAtMillis,
            offsetMinutes = entity.offsetMinutes,
            type = if (entity.type == "ALARM") ReminderType.ALARM else ReminderType.NOTIFICATION,
            isFired = entity.isFired
        )
    }

    private fun domainToEntity(domain: Reminder): ReminderEntity {
        return ReminderEntity(
            id = domain.id,
            taskId = domain.taskId,
            triggerAtMillis = domain.triggerAtMillis,
            offsetMinutes = domain.offsetMinutes,
            type = domain.type.name,
            isFired = domain.isFired
        )
    }
}

class FocusRepositoryImpl(
    private val focusSessionDao: FocusSessionDao,
    private val taskDao: TaskDao
) : FocusRepository {

    override fun getAllSessions(): Flow<List<FocusSession>> {
        return focusSessionDao.getAllSessions().map { list ->
            list.map { FocusSession(it.id, it.taskId, it.startedAt, it.durationSec, it.type) }
        }
    }

    override fun getSessionsForTask(taskId: Long): Flow<List<FocusSession>> {
        return focusSessionDao.getSessionsForTask(taskId).map { list ->
            list.map { FocusSession(it.id, it.taskId, it.startedAt, it.durationSec, it.type) }
        }
    }

    override suspend fun insertSession(session: FocusSession): Long {
        return focusSessionDao.insertSession(
            FocusSessionEntity(
                taskId = session.taskId,
                startedAt = session.startedAt,
                durationSec = session.durationSec,
                type = session.type
            )
        )
    }

    override fun getTodayFocusMinutes(): Flow<Int> {
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return focusSessionDao.getTodayFocusMinutes(cal.timeInMillis).map { it ?: 0 }
    }

    override fun getCompletedTaskCountStats(): Flow<Map<String, Int>> {
        return taskDao.getCompletedTasks().map { tasks ->
            val now = System.currentTimeMillis()
            val cal = Calendar.getInstance().apply {
                timeInMillis = now
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val startToday = cal.timeInMillis

            cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
            val startWeek = cal.timeInMillis

            cal.set(Calendar.DAY_OF_MONTH, 1)
            val startMonth = cal.timeInMillis

            val todayCount = tasks.count { (it.completedAt ?: 0L) >= startToday }
            val weekCount = tasks.count { (it.completedAt ?: 0L) >= startWeek }
            val monthCount = tasks.count { (it.completedAt ?: 0L) >= startMonth }

            mapOf(
                "today" to todayCount,
                "this_week" to weekCount,
                "this_month" to monthCount,
                "total" to tasks.size
            )
        }
    }
}

class TemplateRepositoryImpl(
    private val templateDao: TemplateDao
) : TemplateRepository {
    override fun getAllTemplates(): Flow<List<TaskTemplate>> {
        return templateDao.getAllTemplates().map { list ->
            list.map { TaskTemplate(it.id, it.name, it.payloadJson) }
        }
    }

    override suspend fun insertTemplate(template: TaskTemplate): Long {
        return templateDao.insertTemplate(TemplateEntity(name = template.name, payloadJson = template.payloadJson))
    }

    override suspend fun deleteTemplate(id: Long) {
        templateDao.deleteTemplate(id)
    }
}

class SavedFilterRepositoryImpl(
    private val savedFilterDao: SavedFilterDao
) : SavedFilterRepository {
    override fun getAllFilters(): Flow<List<SavedFilter>> {
        return savedFilterDao.getAllFilters().map { list ->
            list.map { SavedFilter(it.id, it.name, it.queryJson) }
        }
    }

    override suspend fun insertFilter(filter: SavedFilter): Long {
        return savedFilterDao.insertFilter(SavedFilterEntity(name = filter.name, queryJson = filter.queryJson))
    }

    override suspend fun deleteFilter(id: Long) {
        savedFilterDao.deleteFilter(id)
    }
}
