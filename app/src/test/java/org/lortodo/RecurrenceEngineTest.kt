package org.lortodo

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import org.lortodo.domain.engine.RecurrenceEngine
import org.lortodo.domain.model.RecurrenceEndType
import org.lortodo.domain.model.RecurrenceFrequency
import org.lortodo.domain.model.RecurrenceRule
import org.lortodo.domain.model.RepeatFrom
import org.lortodo.domain.model.Task
import java.util.Calendar

class RecurrenceEngineTest {

    @Test
    fun testDailyRecurrence() {
        val cal = Calendar.getInstance().apply {
            set(2026, Calendar.MAY, 8, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val task = Task(
            title = "Drink water",
            dueDate = cal.timeInMillis,
            recurrenceRule = RecurrenceRule(frequency = RecurrenceFrequency.DAILY, interval = 1)
        )

        val nextTask = RecurrenceEngine.createNextOccurrence(task)
        assertNotNull(nextTask)

        val expectedNextCal = (cal.clone() as Calendar).apply {
            add(Calendar.DAY_OF_YEAR, 1)
        }
        assertEquals(expectedNextCal.timeInMillis, nextTask?.dueDate)
        assertEquals(1, nextTask?.recurrenceRule?.occurrencesCount)
    }

    @Test
    fun testWeeklyRecurrenceSpecificDays() {
        // May 8, 2026 is a Friday (ISO day 5)
        val cal = Calendar.getInstance().apply {
            set(2026, Calendar.MAY, 8, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }
        // Recurring on Monday (1) and Friday (5)
        val task = Task(
            title = "Team sync",
            dueDate = cal.timeInMillis,
            recurrenceRule = RecurrenceRule(
                frequency = RecurrenceFrequency.WEEKLY,
                daysOfWeek = listOf(1, 5) // Mon, Fri
            )
        )

        val nextTask = RecurrenceEngine.createNextOccurrence(task)
        assertNotNull(nextTask)

        // After Friday, the next occurrence should be next Monday (May 11, 2026)
        val nextCal = Calendar.getInstance().apply {
            timeInMillis = nextTask!!.dueDate!!
        }
        assertEquals(Calendar.MONDAY, nextCal.get(Calendar.DAY_OF_WEEK))
    }

    @Test
    fun testRecurrenceCountLimit() {
        val task = Task(
            title = "Physical therapy",
            dueDate = System.currentTimeMillis(),
            recurrenceRule = RecurrenceRule(
                frequency = RecurrenceFrequency.DAILY,
                endType = RecurrenceEndType.AFTER_COUNT,
                endCount = 3,
                occurrencesCount = 2 // Already had 2 occurrences
            )
        )

        // This 3rd completion reaches count limit (2 + 1 >= 3)
        val nextTask = RecurrenceEngine.createNextOccurrence(task)
        assertNull(nextTask) // Ended!
    }
}
