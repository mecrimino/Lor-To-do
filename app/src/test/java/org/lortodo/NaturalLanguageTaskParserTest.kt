package org.lortodo

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.lortodo.domain.engine.NaturalLanguageTaskParser
import org.lortodo.domain.model.Priority
import org.lortodo.domain.model.RecurrenceFrequency
import java.util.Calendar

class NaturalLanguageTaskParserTest {

    @Test
    fun testParseFullTaskExpression() {
        val input = "Pay rent tomorrow 6pm !high #bills every month"
        val baseTime = 1700000000000L // arbitrary fixed time
        val result = NaturalLanguageTaskParser.parse(input, baseTime)

        assertEquals("Pay rent", result.title)
        assertEquals(Priority.HIGH, result.priority)
        assertTrue(result.tags.contains("bills"))
        assertNotNull(result.dueDate)
        assertNotNull(result.dueTime)
        assertFalse(result.isAllDay)

        assertNotNull(result.recurrenceRule)
        assertEquals(RecurrenceFrequency.MONTHLY, result.recurrenceRule?.frequency)
    }

    @Test
    fun testParseDayOfWeekAndTime() {
        val input = "Buy groceries next friday at 10am !med #shopping"
        val result = NaturalLanguageTaskParser.parse(input)

        assertEquals("Buy groceries", result.title)
        assertEquals(Priority.MEDIUM, result.priority)
        assertTrue(result.tags.contains("shopping"))
        assertNotNull(result.dueDate)
        assertNotNull(result.dueTime)
        assertFalse(result.isAllDay)
    }

    @Test
    fun testParseSimpleTaskWithoutTokens() {
        val input = "Read chapter 4 of physics textbook"
        val result = NaturalLanguageTaskParser.parse(input)

        assertEquals("Read chapter 4 of physics textbook", result.title)
        assertEquals(Priority.NONE, result.priority)
        assertTrue(result.tags.isEmpty())
        assertTrue(result.isAllDay)
    }

    @Test
    fun testParseRelativeDays() {
        val input = "Clean room in 3 days !low"
        val result = NaturalLanguageTaskParser.parse(input)

        assertEquals("Clean room", result.title)
        assertEquals(Priority.LOW, result.priority)
        assertNotNull(result.dueDate)
    }
}
