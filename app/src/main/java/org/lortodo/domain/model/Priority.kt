package org.lortodo.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class Priority(val level: Int, val label: String) {
    NONE(0, "None"),
    LOW(1, "Low"),
    MEDIUM(2, "Medium"),
    HIGH(3, "High");

    companion object {
        fun fromLevel(level: Int): Priority =
            entries.find { it.level == level } ?: NONE

        fun fromString(str: String?): Priority =
            when (str?.lowercase()?.trim()) {
                "high", "h", "!high", "!h", "3" -> HIGH
                "medium", "med", "m", "!med", "!m", "2" -> MEDIUM
                "low", "l", "!low", "!l", "1" -> LOW
                else -> NONE
            }
    }
}
