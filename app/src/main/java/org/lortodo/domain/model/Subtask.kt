package org.lortodo.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Subtask(
    val id: Long = 0,
    val taskId: Long,
    val title: String,
    val isDone: Boolean = false,
    val sortOrder: Int = 0
)
