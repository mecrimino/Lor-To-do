package org.lortodo.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class TaskList(
    val id: Long = 0,
    val name: String,
    val colorHex: String = "#E5A800",
    val icon: String = "inbox",
    val folderId: Long? = null,
    val sortOrder: Int = 0,
    val isArchived: Boolean = false,
    val taskCount: Int = 0
)

@Serializable
data class Tag(
    val id: Long = 0,
    val name: String,
    val colorHex: String = "#7A7570"
)

@Serializable
enum class ReminderType {
    NOTIFICATION,
    ALARM
}

@Serializable
data class Reminder(
    val id: Long = 0,
    val taskId: Long,
    val triggerAtMillis: Long,
    val offsetMinutes: Int = 0, // e.g. 0 = at time, 10 = 10 min before, 60 = 1h before
    val type: ReminderType = ReminderType.NOTIFICATION,
    val isFired: Boolean = false
)

@Serializable
data class FocusSession(
    val id: Long = 0,
    val taskId: Long? = null,
    val startedAt: Long = System.currentTimeMillis(),
    val durationSec: Int = 1500, // 25 mins default
    val type: String = "work" // work, short_break, long_break
)

@Serializable
data class TaskTemplate(
    val id: Long = 0,
    val name: String,
    val payloadJson: String
)

@Serializable
data class SavedFilter(
    val id: Long = 0,
    val name: String,
    val queryJson: String
)
