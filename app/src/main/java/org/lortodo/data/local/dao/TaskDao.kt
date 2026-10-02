package org.lortodo.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import org.lortodo.data.local.entity.TaskEntity
import org.lortodo.data.local.entity.TaskTagCrossRef

@Dao
interface TaskDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity): Long

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Query("SELECT * FROM tasks WHERE id = :taskId")
    fun getTaskById(taskId: Long): Flow<TaskEntity?>

    @Query("SELECT * FROM tasks WHERE id = :taskId")
    suspend fun getTaskByIdOnce(taskId: Long): TaskEntity?

    // Active tasks (not in trash and not archived)
    @Query("""
        SELECT * FROM tasks 
        WHERE deletedAt IS NULL AND archivedAt IS NULL
        ORDER BY sortOrder ASC, createdAt DESC
    """)
    fun getAllActiveTasks(): Flow<List<TaskEntity>>

    // Smart View: Inbox (default listId = 1)
    @Query("""
        SELECT * FROM tasks 
        WHERE deletedAt IS NULL AND archivedAt IS NULL AND listId = :listId
        ORDER BY isCompleted ASC, sortOrder ASC, createdAt DESC
    """)
    fun getTasksByList(listId: Long): Flow<List<TaskEntity>>

    // Smart View: Today (due today or overdue and not completed)
    @Query("""
        SELECT * FROM tasks 
        WHERE deletedAt IS NULL AND archivedAt IS NULL 
        AND dueDate IS NOT NULL AND dueDate <= :endOfDayMillis
        ORDER BY isCompleted ASC, isStarred DESC, dueDate ASC, priority DESC
    """)
    fun getTodayTasks(endOfDayMillis: Long): Flow<List<TaskEntity>>

    // Smart View: Tomorrow
    @Query("""
        SELECT * FROM tasks 
        WHERE deletedAt IS NULL AND archivedAt IS NULL 
        AND dueDate IS NOT NULL AND dueDate >= :startTomorrowMillis AND dueDate <= :endTomorrowMillis
        ORDER BY isCompleted ASC, isStarred DESC, dueDate ASC, priority DESC
    """)
    fun getTomorrowTasks(startTomorrowMillis: Long, endTomorrowMillis: Long): Flow<List<TaskEntity>>

    // Smart View: Upcoming (from start of today until 7 days ahead)
    @Query("""
        SELECT * FROM tasks 
        WHERE deletedAt IS NULL AND archivedAt IS NULL 
        AND dueDate IS NOT NULL AND dueDate >= :startTodayMillis AND dueDate <= :endUpcomingMillis
        ORDER BY isCompleted ASC, dueDate ASC, priority DESC
    """)
    fun getUpcomingTasks(startTodayMillis: Long, endUpcomingMillis: Long): Flow<List<TaskEntity>>

    // Smart View: Overdue
    @Query("""
        SELECT * FROM tasks 
        WHERE deletedAt IS NULL AND archivedAt IS NULL 
        AND isCompleted = 0 
        AND dueDate IS NOT NULL AND dueDate < :startTodayMillis
        ORDER BY dueDate ASC, priority DESC
    """)
    fun getOverdueTasks(startTodayMillis: Long): Flow<List<TaskEntity>>

    // Smart View: Starred
    @Query("""
        SELECT * FROM tasks 
        WHERE deletedAt IS NULL AND archivedAt IS NULL AND isStarred = 1
        ORDER BY isCompleted ASC, sortOrder ASC, createdAt DESC
    """)
    fun getStarredTasks(): Flow<List<TaskEntity>>

    // Smart View: Completed
    @Query("""
        SELECT * FROM tasks 
        WHERE deletedAt IS NULL AND isCompleted = 1
        ORDER BY completedAt DESC, updatedAt DESC
    """)
    fun getCompletedTasks(): Flow<List<TaskEntity>>

    // Smart View: No Date
    @Query("""
        SELECT * FROM tasks 
        WHERE deletedAt IS NULL AND archivedAt IS NULL AND dueDate IS NULL
        ORDER BY isCompleted ASC, sortOrder ASC, createdAt DESC
    """)
    fun getNoDateTasks(): Flow<List<TaskEntity>>

    // Specific date tasks (for calendar)
    @Query("""
        SELECT * FROM tasks 
        WHERE deletedAt IS NULL AND archivedAt IS NULL 
        AND dueDate >= :startOfDayMillis AND dueDate <= :endOfDayMillis
        ORDER BY isCompleted ASC, dueDate ASC
    """)
    fun getTasksForDate(startOfDayMillis: Long, endOfDayMillis: Long): Flow<List<TaskEntity>>

    // Date range tasks
    @Query("""
        SELECT * FROM tasks 
        WHERE deletedAt IS NULL AND archivedAt IS NULL 
        AND dueDate >= :startMillis AND dueDate <= :endMillis
        ORDER BY dueDate ASC
    """)
    fun getTasksForDateRange(startMillis: Long, endMillis: Long): Flow<List<TaskEntity>>

    // Smart View: Trash
    @Query("SELECT * FROM tasks WHERE deletedAt IS NOT NULL ORDER BY deletedAt DESC")
    fun getTrashTasks(): Flow<List<TaskEntity>>

    // Smart View: Archive
    @Query("SELECT * FROM tasks WHERE archivedAt IS NOT NULL AND deletedAt IS NULL ORDER BY archivedAt DESC")
    fun getArchivedTasks(): Flow<List<TaskEntity>>

    // FTS Full text search
    @Query("""
        SELECT tasks.* FROM tasks 
        JOIN tasks_fts ON tasks.id = tasks_fts.rowid
        WHERE tasks_fts MATCH :query AND tasks.deletedAt IS NULL
        ORDER BY tasks.isCompleted ASC, tasks.updatedAt DESC
    """)
    fun searchTasks(query: String): Flow<List<TaskEntity>>

    // Fallback LIKE search if FTS syntax query is plain string
    @Query("""
        SELECT * FROM tasks 
        WHERE (title LIKE '%' || :query || '%' OR notes LIKE '%' || :query || '%')
        AND deletedAt IS NULL
        ORDER BY isCompleted ASC, updatedAt DESC
    """)
    fun searchTasksLike(query: String): Flow<List<TaskEntity>>

    // State mutations
    @Query("UPDATE tasks SET isCompleted = :isCompleted, completedAt = :completedAt, updatedAt = :updatedAt WHERE id = :taskId")
    suspend fun setTaskCompleted(taskId: Long, isCompleted: Boolean, completedAt: Long?, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE tasks SET isStarred = NOT isStarred, updatedAt = :updatedAt WHERE id = :taskId")
    suspend fun toggleTaskStarred(taskId: Long, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE tasks SET deletedAt = :deletedAt, updatedAt = :deletedAt WHERE id = :taskId")
    suspend fun softDeleteTask(taskId: Long, deletedAt: Long = System.currentTimeMillis())

    @Query("UPDATE tasks SET deletedAt = NULL, updatedAt = :updatedAt WHERE id = :taskId")
    suspend fun restoreTask(taskId: Long, updatedAt: Long = System.currentTimeMillis())

    @Query("DELETE FROM tasks WHERE id = :taskId")
    suspend fun permanentlyDeleteTask(taskId: Long)

    @Query("DELETE FROM tasks WHERE deletedAt IS NOT NULL")
    suspend fun emptyTrash()

    @Query("DELETE FROM tasks WHERE deletedAt IS NOT NULL AND deletedAt < :thresholdMillis")
    suspend fun purgeDeletedTasksOlderThan(thresholdMillis: Long)

    @Query("UPDATE tasks SET archivedAt = :archivedAt, updatedAt = :archivedAt WHERE id = :taskId")
    suspend fun archiveTask(taskId: Long, archivedAt: Long = System.currentTimeMillis())

    @Query("UPDATE tasks SET archivedAt = NULL, updatedAt = :updatedAt WHERE id = :taskId")
    suspend fun unarchiveTask(taskId: Long, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE tasks SET sortOrder = :newOrder WHERE id = :taskId")
    suspend fun updateTaskSortOrder(taskId: Long, newOrder: Int)

    // Tag association cross refs
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTaskTagCrossRefs(refs: List<TaskTagCrossRef>)

    @Query("DELETE FROM task_tag_cross_ref WHERE taskId = :taskId")
    suspend fun deleteTagCrossRefsForTask(taskId: Long)

    @Query("SELECT tagId FROM task_tag_cross_ref WHERE taskId = :taskId")
    suspend fun getTagIdsForTask(taskId: Long): List<Long>

    @Transaction
    suspend fun setTagsForTask(taskId: Long, tagIds: List<Long>) {
        deleteTagCrossRefsForTask(taskId)
        if (tagIds.isNotEmpty()) {
            val refs = tagIds.map { TaskTagCrossRef(taskId = taskId, tagId = it) }
            insertTaskTagCrossRefs(refs)
        }
    }
}
