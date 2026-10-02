package org.lortodo.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class RecurrenceFrequency {
    DAILY,
    WEEKLY,
    MONTHLY,
    YEARLY,
    CUSTOM
}

@Serializable
enum class RepeatFrom {
    DUE_DATE,
    COMPLETION_DATE
}

@Serializable
data class RecurrenceRule(
    val frequency: RecurrenceFrequency = RecurrenceFrequency.DAILY,
    val interval: Int = 1, // e.g. every 2 weeks
    val daysOfWeek: List<Int> = emptyList(), // 1 = Monday, 7 = Sunday (ISO-8601)
    val dayOfMonth: Int? = null, // e.g. 15th of the month
    val endType: RecurrenceEndType = RecurrenceEndType.NEVER,
    val endDateMillis: Long? = null,
    val endCount: Int? = null,
    val occurrencesCount: Int = 0,
    val repeatFrom: RepeatFrom = RepeatFrom.DUE_DATE
) {
    fun toReadableString(): String {
        return when (frequency) {
            RecurrenceFrequency.DAILY -> if (interval == 1) "Daily" else "Every $interval days"
            RecurrenceFrequency.WEEKLY -> {
                if (daysOfWeek.isEmpty()) {
                    if (interval == 1) "Weekly" else "Every $interval weeks"
                } else {
                    val dayNames = daysOfWeek.map { dayNumberToShortName(it) }.joinToString(", ")
                    if (interval == 1) "Weekly on $dayNames" else "Every $interval weeks on $dayNames"
                }
            }
            RecurrenceFrequency.MONTHLY -> if (interval == 1) "Monthly" else "Every $interval months"
            RecurrenceFrequency.YEARLY -> if (interval == 1) "Yearly" else "Every $interval years"
            RecurrenceFrequency.CUSTOM -> "Every $interval units"
        }
    }

    private fun dayNumberToShortName(day: Int): String {
        return when (day) {
            1 -> "Mon"
            2 -> "Tue"
            3 -> "Wed"
            4 -> "Thu"
            5 -> "Fri"
            6 -> "Sat"
            7 -> "Sun"
            else -> ""
        }
    }
}

@Serializable
enum class RecurrenceEndType {
    NEVER,
    ON_DATE,
    AFTER_COUNT
}
