package org.lortodo.data.backup

import kotlinx.serialization.Serializable
import org.lortodo.domain.model.Priority
import org.lortodo.domain.model.RecurrenceRule
import org.lortodo.domain.model.RepeatFrom
import org.lortodo.domain.model.UserPreferences

@Serializable
data class BackupPayload(
    val version: Int = 1,
    val app: String = "LOR-TODO",
    val exportedAt: Long = System.currentTimeMillis(),
    val tasks: List<BackupTask> = emptyList(),
    val lists: List<BackupTaskList> = emptyList(),
    val tags: List<BackupTag> = emptyList(),
    val taskTagRefs: List<BackupTaskTagRef> = emptyList(),
    val preferences: UserPreferences? = null
)

@Serializable
data class BackupTask(
    val id: Long,
    val title: String,
    val notes: String = "",
    val listId: Long = 1L,
    val priority: Priority = Priority.NONE,
    val isCompleted: Boolean = false,
    val completedAt: Long? = null,
    val dueDate: Long? = null,
    val dueTime: Long? = null,
    val isAllDay: Boolean = true,
    val isStarred: Boolean = false,
    val recurrenceRule: RecurrenceRule? = null,
    val repeatFrom: RepeatFrom = RepeatFrom.DUE_DATE,
    val estimateMinutes: Int? = null,
    val sortOrder: Int = 0,
    val createdAt: Long,
    val updatedAt: Long,
    val subtasks: List<BackupSubtask> = emptyList()
)

@Serializable
data class BackupSubtask(
    val title: String,
    val isDone: Boolean = false,
    val sortOrder: Int = 0
)

@Serializable
data class BackupTaskList(
    val id: Long,
    val name: String,
    val colorHex: String,
    val icon: String,
    val sortOrder: Int = 0,
    val isArchived: Boolean = false
)

@Serializable
data class BackupTag(
    val id: Long,
    val name: String,
    val colorHex: String
)

@Serializable
data class BackupTaskTagRef(
    val taskId: Long,
    val tagId: Long
)
