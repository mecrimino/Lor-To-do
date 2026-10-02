package org.lortodo.domain.engine

import org.lortodo.domain.model.RecurrenceEndType
import org.lortodo.domain.model.RecurrenceFrequency
import org.lortodo.domain.model.RecurrenceRule
import org.lortodo.domain.model.RepeatFrom
import org.lortodo.domain.model.Task
import java.util.Calendar
import java.util.TimeZone

object RecurrenceEngine {

    /**
     * Computes the next occurrence of a recurring task when completed.
     * Returns a new Task with updated due date, reset completion status,
     * and incremented recurrence count, or null if the recurrence rule has ended.
     */
    fun createNextOccurrence(task: Task, completionTimeMillis: Long = System.currentTimeMillis()): Task? {
        val rule = task.recurrenceRule ?: return null

        // Check occurrence count limit
        if (rule.endType == RecurrenceEndType.AFTER_COUNT) {
            val maxCount = rule.endCount ?: 1
            if (rule.occurrencesCount + 1 >= maxCount) {
                return null // Completed all occurrences
            }
        }

        // Determine base date: Repeat from due date vs repeat from completion date
        val baseDateMillis = when (rule.repeatFrom) {
            RepeatFrom.DUE_DATE -> task.dueDate ?: completionTimeMillis
            RepeatFrom.COMPLETION_DATE -> completionTimeMillis
        }

        val nextDueDateMillis = calculateNextDueDate(baseDateMillis, rule)

        // Check end date limit
        if (rule.endType == RecurrenceEndType.ON_DATE) {
            val endLimit = rule.endDateMillis
            if (endLimit != null && nextDueDateMillis > endLimit) {
                return null // Passed end date
            }
        }

        val updatedRule = rule.copy(
            occurrencesCount = rule.occurrencesCount + 1
        )

        // Reset subtasks for recurring task
        val resetSubtasks = task.subtasks.map { it.copy(id = 0, isDone = false) }

        return task.copy(
            id = 0, // New task entry
            isCompleted = false,
            completedAt = null,
            dueDate = nextDueDateMillis,
            recurrenceRule = updatedRule,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            subtasks = resetSubtasks
        )
    }

    /**
     * Calculates the next due date timestamp (in UTC epoch millis at start of day) based on recurrence rule.
     */
    fun calculateNextDueDate(
        baseMillis: Long,
        rule: RecurrenceRule,
        timeZone: TimeZone = TimeZone.getDefault()
    ): Long {
        val cal = Calendar.getInstance(timeZone).apply {
            timeInMillis = baseMillis
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val interval = if (rule.interval > 0) rule.interval else 1

        when (rule.frequency) {
            RecurrenceFrequency.DAILY -> {
                cal.add(Calendar.DAY_OF_YEAR, interval)
            }
            RecurrenceFrequency.WEEKLY -> {
                if (rule.daysOfWeek.isEmpty()) {
                    cal.add(Calendar.WEEK_OF_YEAR, interval)
                } else {
                    // Find next selected day of week
                    // ISO days: 1 = Mon, 2 = Tue, ..., 7 = Sun
                    // Calendar days: Sun = 1, Mon = 2, ..., Sat = 7
                    val sortedIsoDays = rule.daysOfWeek.sorted()
                    var found = false
                    val currentDayOfWeekIso = toIsoDayOfWeek(cal.get(Calendar.DAY_OF_WEEK))

                    // Check later days in the same week
                    for (day in sortedIsoDays) {
                        if (day > currentDayOfWeekIso) {
                            val diff = day - currentDayOfWeekIso
                            cal.add(Calendar.DAY_OF_YEAR, diff)
                            found = true
                            break
                        }
                    }

                    if (!found) {
                        // Advance by interval weeks and jump to the first selected day
                        val firstDayIso = sortedIsoDays.first()
                        val daysUntilNextWeek = 7 - currentDayOfWeekIso
                        cal.add(Calendar.DAY_OF_YEAR, daysUntilNextWeek) // Now at end of current week (next Monday)
                        if (interval > 1) {
                            cal.add(Calendar.WEEK_OF_YEAR, interval - 1)
                        }
                        val diff = firstDayIso - 1 // Days from Monday
                        cal.add(Calendar.DAY_OF_YEAR, diff)
                    }
                }
            }
            RecurrenceFrequency.MONTHLY -> {
                if (rule.dayOfMonth != null) {
                    cal.add(Calendar.MONTH, interval)
                    val maxDayInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
                    cal.set(Calendar.DAY_OF_MONTH, minOf(rule.dayOfMonth, maxDayInMonth))
                } else {
                    cal.add(Calendar.MONTH, interval)
                }
            }
            RecurrenceFrequency.YEARLY -> {
                cal.add(Calendar.YEAR, interval)
            }
            RecurrenceFrequency.CUSTOM -> {
                cal.add(Calendar.DAY_OF_YEAR, interval)
            }
        }

        return cal.timeInMillis
    }

    private fun toIsoDayOfWeek(calendarDay: Int): Int {
        return when (calendarDay) {
            Calendar.MONDAY -> 1
            Calendar.TUESDAY -> 2
            Calendar.WEDNESDAY -> 3
            Calendar.THURSDAY -> 4
            Calendar.FRIDAY -> 5
            Calendar.SATURDAY -> 6
            Calendar.SUNDAY -> 7
            else -> 1
        }
    }
}
