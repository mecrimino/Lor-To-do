package org.lortodo.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import org.lortodo.data.local.entity.FocusSessionEntity
import org.lortodo.data.local.entity.ReminderEntity
import org.lortodo.data.local.entity.SavedFilterEntity
import org.lortodo.data.local.entity.SubtaskEntity
import org.lortodo.data.local.entity.TagEntity
import org.lortodo.data.local.entity.TaskListEntity
import org.lortodo.data.local.entity.TemplateEntity

@Dao
interface SubtaskDao {
    @Query("SELECT * FROM subtasks WHERE taskId = :taskId ORDER BY sortOrder ASC, id ASC")
    fun getSubtasksForTask(taskId: Long): Flow<List<SubtaskEntity>>

    @Query("SELECT * FROM subtasks WHERE taskId = :taskId ORDER BY sortOrder ASC, id ASC")
    suspend fun getSubtasksForTaskOnce(taskId: Long): List<SubtaskEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubtask(subtask: SubtaskEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubtasks(subtasks: List<SubtaskEntity>)

    @Update
    suspend fun updateSubtask(subtask: SubtaskEntity)

    @Query("DELETE FROM subtasks WHERE id = :id")
    suspend fun deleteSubtask(id: Long)

    @Query("UPDATE subtasks SET isDone = NOT isDone WHERE id = :id")
    suspend fun toggleSubtask(id: Long)
}

@Dao
interface TaskListDao {
    @Query("SELECT * FROM task_lists WHERE isArchived = 0 ORDER BY sortOrder ASC, id ASC")
    fun getAllActiveLists(): Flow<List<TaskListEntity>>

    @Query("SELECT * FROM task_lists ORDER BY sortOrder ASC, id ASC")
    fun getAllLists(): Flow<List<TaskListEntity>>

    @Query("SELECT * FROM task_lists WHERE id = :id")
    fun getListById(id: Long): Flow<TaskListEntity?>

    @Query("SELECT * FROM task_lists WHERE id = :id")
    suspend fun getListByIdOnce(id: Long): TaskListEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertList(list: TaskListEntity): Long

    @Update
    suspend fun updateList(list: TaskListEntity)

    @Query("DELETE FROM task_lists WHERE id = :id")
    suspend fun deleteList(id: Long)

    @Query("UPDATE task_lists SET isArchived = :isArchived WHERE id = :id")
    suspend fun archiveList(id: Long, isArchived: Boolean)
}

@Dao
interface TagDao {
    @Query("SELECT * FROM tags ORDER BY name ASC")
    fun getAllTags(): Flow<List<TagEntity>>

    @Query("SELECT * FROM tags WHERE id = :id")
    fun getTagById(id: Long): Flow<TagEntity?>

    @Query("SELECT * FROM tags WHERE name = :name LIMIT 1")
    suspend fun getTagByName(name: String): TagEntity?

    @Query("""
        SELECT tags.* FROM tags 
        JOIN task_tag_cross_ref ON tags.id = task_tag_cross_ref.tagId 
        WHERE task_tag_cross_ref.taskId = :taskId
    """)
    fun getTagsForTask(taskId: Long): Flow<List<TagEntity>>

    @Query("""
        SELECT tags.* FROM tags 
        JOIN task_tag_cross_ref ON tags.id = task_tag_cross_ref.tagId 
        WHERE task_tag_cross_ref.taskId = :taskId
    """)
    suspend fun getTagsForTaskOnce(taskId: Long): List<TagEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTag(tag: TagEntity): Long

    @Update
    suspend fun updateTag(tag: TagEntity)

    @Query("DELETE FROM tags WHERE id = :id")
    suspend fun deleteTag(id: Long)
}

@Dao
interface ReminderDao {
    @Query("SELECT * FROM reminders WHERE taskId = :taskId ORDER BY triggerAtMillis ASC")
    fun getRemindersForTask(taskId: Long): Flow<List<ReminderEntity>>

    @Query("SELECT * FROM reminders WHERE taskId = :taskId ORDER BY triggerAtMillis ASC")
    suspend fun getRemindersForTaskOnce(taskId: Long): List<ReminderEntity>

    @Query("SELECT * FROM reminders WHERE isFired = 0 ORDER BY triggerAtMillis ASC")
    fun getAllActiveReminders(): Flow<List<ReminderEntity>>

    @Query("SELECT * FROM reminders WHERE isFired = 0 ORDER BY triggerAtMillis ASC")
    suspend fun getAllActiveRemindersOnce(): List<ReminderEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: ReminderEntity): Long

    @Update
    suspend fun updateReminder(reminder: ReminderEntity)

    @Query("UPDATE reminders SET isFired = 1 WHERE id = :id")
    suspend fun markReminderFired(id: Long)

    @Query("DELETE FROM reminders WHERE id = :id")
    suspend fun deleteReminder(id: Long)

    @Query("DELETE FROM reminders WHERE taskId = :taskId")
    suspend fun deleteRemindersForTask(taskId: Long)
}

@Dao
interface FocusSessionDao {
    @Query("SELECT * FROM focus_sessions ORDER BY startedAt DESC")
    fun getAllSessions(): Flow<List<FocusSessionEntity>>

    @Query("SELECT * FROM focus_sessions WHERE taskId = :taskId ORDER BY startedAt DESC")
    fun getSessionsForTask(taskId: Long): Flow<List<FocusSessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: FocusSessionEntity): Long

    @Query("SELECT SUM(durationSec) / 60 FROM focus_sessions WHERE startedAt >= :startOfDayMillis")
    fun getTodayFocusMinutes(startOfDayMillis: Long): Flow<Int?>
}

@Dao
interface TemplateDao {
    @Query("SELECT * FROM templates ORDER BY id DESC")
    fun getAllTemplates(): Flow<List<TemplateEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTemplate(template: TemplateEntity): Long

    @Query("DELETE FROM templates WHERE id = :id")
    suspend fun deleteTemplate(id: Long)
}

@Dao
interface SavedFilterDao {
    @Query("SELECT * FROM saved_filters ORDER BY id ASC")
    fun getAllFilters(): Flow<List<SavedFilterEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFilter(filter: SavedFilterEntity): Long

    @Query("DELETE FROM saved_filters WHERE id = :id")
    suspend fun deleteFilter(id: Long)
}
