package org.lortodo.domain.repository

import kotlinx.coroutines.flow.Flow
import org.lortodo.domain.model.FocusSession
import org.lortodo.domain.model.Reminder
import org.lortodo.domain.model.SavedFilter
import org.lortodo.domain.model.Tag
import org.lortodo.domain.model.TaskList
import org.lortodo.domain.model.TaskTemplate

interface TaskListRepository {
    fun getAllLists(): Flow<List<TaskList>>
    fun getListById(id: Long): Flow<TaskList?>
    suspend fun insertList(list: TaskList): Long
    suspend fun updateList(list: TaskList)
    suspend fun deleteList(id: Long)
    suspend fun archiveList(id: Long, isArchived: Boolean)
}

interface TagRepository {
    fun getAllTags(): Flow<List<Tag>>
    fun getTagById(id: Long): Flow<Tag?>
    suspend fun insertTag(tag: Tag): Long
    suspend fun updateTag(tag: Tag)
    suspend fun deleteTag(id: Long)
    suspend fun getOrCreateTag(name: String, colorHex: String = "#E5A800"): Tag
}

interface ReminderRepository {
    fun getRemindersForTask(taskId: Long): Flow<List<Reminder>>
    fun getAllActiveReminders(): Flow<List<Reminder>>
    suspend fun insertReminder(reminder: Reminder): Long
    suspend fun updateReminder(reminder: Reminder)
    suspend fun deleteReminder(id: Long)
    suspend fun deleteRemindersForTask(taskId: Long)
    suspend fun markReminderFired(id: Long)
}

interface FocusRepository {
    fun getAllSessions(): Flow<List<FocusSession>>
    fun getSessionsForTask(taskId: Long): Flow<List<FocusSession>>
    suspend fun insertSession(session: FocusSession): Long
    fun getTodayFocusMinutes(): Flow<Int>
    fun getCompletedTaskCountStats(): Flow<Map<String, Int>> // e.g. "today" -> 5, "this_week" -> 20
}

interface TemplateRepository {
    fun getAllTemplates(): Flow<List<TaskTemplate>>
    suspend fun insertTemplate(template: TaskTemplate): Long
    suspend fun deleteTemplate(id: Long)
}

interface SavedFilterRepository {
    fun getAllFilters(): Flow<List<SavedFilter>>
    suspend fun insertFilter(filter: SavedFilter): Long
    suspend fun deleteFilter(id: Long)
}
