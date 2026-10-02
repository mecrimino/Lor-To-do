package org.lortodo.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Task(
    val id: Long = 0,
    val title: String,
    val notes: String = "",
    val listId: Long = 1, // Default Inbox
    val priority: Priority = Priority.NONE,
    val isCompleted: Boolean = false,
    val completedAt: Long? = null,
    val dueDate: Long? = null, // epoch millis (start of day)
    val dueTime: Long? = null, // millis from start of day, or specific epoch millis
    val isAllDay: Boolean = true,
    val isStarred: Boolean = false,
    val recurrenceRule: RecurrenceRule? = null,
    val repeatFrom: RepeatFrom = RepeatFrom.DUE_DATE,
    val estimateMinutes: Int? = null,
    val sortOrder: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val deletedAt: Long? = null, // Soft delete (Trash)
    val archivedAt: Long? = null, // Archive
    // In-memory / composite joined properties:
    val subtasks: List<Subtask> = emptyList(),
    val tags: List<Tag> = emptyList(),
    val reminders: List<Reminder> = emptyList(),
    val listName: String? = null
) {
    val isDeleted: Boolean get() = deletedAt != null
    val isArchived: Boolean get() = archivedAt != null

    val isOverdue: Boolean
        get() {
            if (isCompleted || dueDate == null) return false
            val now = System.currentTimeMillis()
            return if (isAllDay) {
                // If it's all day, compare to end of due date (or current day start)
                val dayEnd = dueDate + (24 * 60 * 60 * 1000 - 1)
                now > dayEnd
            } else {
                val targetTime = (dueDate) + (dueTime ?: 0L)
                now > targetTime
            }
        }

    val subtasksCompletedCount: Int
        get() = subtasks.count { it.isDone }

    val subtasksTotalCount: Int
        get() = subtasks.size
}
