package org.lortodo.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Fts4
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "tasks",
    indices = [
        Index("listId"),
        Index("dueDate"),
        Index("isCompleted"),
        Index("deletedAt"),
        Index("archivedAt")
    ]
)
data class TaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val notes: String = "",
    val listId: Long = 1L,
    val priority: Int = 0,
    val isCompleted: Boolean = false,
    val completedAt: Long? = null,
    val dueDate: Long? = null,
    val dueTime: Long? = null,
    val isAllDay: Boolean = true,
    val isStarred: Boolean = false,
    val recurrenceRuleJson: String? = null,
    val repeatFrom: String = "DUE_DATE",
    val estimateMinutes: Int? = null,
    val sortOrder: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val deletedAt: Long? = null,
    val archivedAt: Long? = null
)

@Fts4(contentEntity = TaskEntity::class)
@Entity(tableName = "tasks_fts")
data class TaskFtsEntity(
    @ColumnInfo(name = "rowid")
    @PrimaryKey
    val rowid: Long,
    val title: String,
    val notes: String
)

@Entity(
    tableName = "subtasks",
    foreignKeys = [
        ForeignKey(
            entity = TaskEntity::class,
            parentColumns = ["id"],
            childColumns = ["taskId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("taskId")]
)
data class SubtaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val taskId: Long,
    val title: String,
    val isDone: Boolean = false,
    val sortOrder: Int = 0
)

@Entity(tableName = "task_lists")
data class TaskListEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val colorHex: String = "#E5A800",
    val icon: String = "inbox",
    val folderId: Long? = null,
    val sortOrder: Int = 0,
    val isArchived: Boolean = false
)

@Entity(
    tableName = "tags",
    indices = [Index(value = ["name"], unique = true)]
)
data class TagEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val colorHex: String = "#E5A800"
)

@Entity(
    tableName = "task_tag_cross_ref",
    primaryKeys = ["taskId", "tagId"],
    foreignKeys = [
        ForeignKey(
            entity = TaskEntity::class,
            parentColumns = ["id"],
            childColumns = ["taskId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = TagEntity::class,
            parentColumns = ["id"],
            childColumns = ["tagId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("tagId")]
)
data class TaskTagCrossRef(
    val taskId: Long,
    val tagId: Long
)

@Entity(
    tableName = "reminders",
    foreignKeys = [
        ForeignKey(
            entity = TaskEntity::class,
            parentColumns = ["id"],
            childColumns = ["taskId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("taskId"), Index("triggerAtMillis")]
)
data class ReminderEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val taskId: Long,
    val triggerAtMillis: Long,
    val offsetMinutes: Int = 0,
    val type: String = "NOTIFICATION",
    val isFired: Boolean = false
)

@Entity(tableName = "focus_sessions")
data class FocusSessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val taskId: Long? = null,
    val startedAt: Long = System.currentTimeMillis(),
    val durationSec: Int = 1500,
    val type: String = "work"
)

@Entity(tableName = "templates")
data class TemplateEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val payloadJson: String
)

@Entity(tableName = "saved_filters")
data class SavedFilterEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val queryJson: String
)
