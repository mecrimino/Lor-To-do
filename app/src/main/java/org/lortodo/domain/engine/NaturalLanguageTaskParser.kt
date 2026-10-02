package org.lortodo.domain.engine

import org.lortodo.domain.model.Priority
import org.lortodo.domain.model.RecurrenceFrequency
import org.lortodo.domain.model.RecurrenceRule
import java.util.Calendar
import java.util.Locale
import java.util.regex.Pattern

data class ParsedTaskResult(
    val title: String,
    val dueDate: Long? = null, // Start of day millis
    val dueTime: Long? = null, // Millis into day
    val isAllDay: Boolean = true,
    val priority: Priority = Priority.NONE,
    val tags: List<String> = emptyList(),
    val recurrenceRule: RecurrenceRule? = null
)

object NaturalLanguageTaskParser {

    private val PRIORITY_REGEX = Pattern.compile("!(high|urgent|med|medium|low|none|h|m|l)\\b", Pattern.CASE_INSENSITIVE)
    private val TAG_REGEX = Pattern.compile("#([a-zA-Z0-9_-]+)", Pattern.CASE_INSENSITIVE)
    private val RECURRENCE_REGEX = Pattern.compile("\\b(every\\s+(day|week|month|year)|daily|weekly|monthly|yearly)\\b", Pattern.CASE_INSENSITIVE)
    private val TIME_REGEX = Pattern.compile("\\b(?:at\\s+)?([01]?\\d|2[0-3])(?::([0-5]\\d))?\\s*(am|pm)\\b|\\b(?:at\\s+)?([01]?\\d|2[0-3]):([0-5]\\d)\\b|\\b(noon|midnight)\\b", Pattern.CASE_INSENSITIVE)
    private val RELATIVE_DAYS_REGEX = Pattern.compile("\\b(today|tomorrow|tmrw|tonight|in\\s+(\\d+)\\s+days?)\\b", Pattern.CASE_INSENSITIVE)
    private val DAY_OF_WEEK_REGEX = Pattern.compile("\\b(?:next\\s+|on\\s+)?(monday|tuesday|wednesday|thursday|friday|saturday|sunday|mon|tue|wed|thu|fri|sat|sun)\\b", Pattern.CASE_INSENSITIVE)
    private val MONTH_DATE_REGEX = Pattern.compile("\\b(?:on\\s+)?(jan|feb|mar|apr|may|jun|jul|aug|sep|sept|oct|nov|dec|january|february|march|april|june|july|august|september|october|november|december)\\s+(\\d{1,2})(?:st|nd|rd|th)?\\b", Pattern.CASE_INSENSITIVE)

    fun parse(input: String, nowMillis: Long = System.currentTimeMillis()): ParsedTaskResult {
        if (input.isBlank()) {
            return ParsedTaskResult(title = "")
        }

        var text = input.trim()

        // 1. Parse Priority
        var priority = Priority.NONE
        val priorityMatcher = PRIORITY_REGEX.matcher(text)
        if (priorityMatcher.find()) {
            val matched = priorityMatcher.group(1).lowercase(Locale.ROOT)
            priority = when (matched) {
                "high", "urgent", "h" -> Priority.HIGH
                "med", "medium", "m" -> Priority.MEDIUM
                "low", "l" -> Priority.LOW
                else -> Priority.NONE
            }
            text = priorityMatcher.replaceFirst("").trim()
        }

        // 2. Parse Tags
        val tags = mutableListOf<String>()
        val tagMatcher = TAG_REGEX.matcher(text)
        while (tagMatcher.find()) {
            tags.add(tagMatcher.group(1))
        }
        text = TAG_REGEX.matcher(text).replaceAll("").trim()

        // 3. Parse Recurrence
        var recurrenceRule: RecurrenceRule? = null
        val recurrenceMatcher = RECURRENCE_REGEX.matcher(text)
        if (recurrenceMatcher.find()) {
            val token = recurrenceMatcher.group(1).lowercase(Locale.ROOT)
            recurrenceRule = when {
                token.contains("day") || token == "daily" -> RecurrenceRule(frequency = RecurrenceFrequency.DAILY)
                token.contains("week") || token == "weekly" -> RecurrenceRule(frequency = RecurrenceFrequency.WEEKLY)
                token.contains("month") || token == "monthly" -> RecurrenceRule(frequency = RecurrenceFrequency.MONTHLY)
                token.contains("year") || token == "yearly" -> RecurrenceRule(frequency = RecurrenceFrequency.YEARLY)
                else -> null
            }
            text = recurrenceMatcher.replaceFirst("").trim()
        }

        // 4. Parse Time
        var dueTimeMillis: Long? = null
        var isAllDay = true
        val timeMatcher = TIME_REGEX.matcher(text)
        if (timeMatcher.find()) {
            val hourStr = timeMatcher.group(1)
            val minStr = timeMatcher.group(2)
            val amPm = timeMatcher.group(3)
            val h24Str = timeMatcher.group(4)
            val m24Str = timeMatcher.group(5)
            val word = timeMatcher.group(6)

            var hour = 0
            var minute = 0

            when {
                word != null -> {
                    if (word.equals("noon", ignoreCase = true)) {
                        hour = 12
                    } else if (word.equals("midnight", ignoreCase = true)) {
                        hour = 0
                    }
                }
                amPm != null -> {
                    var h = hourStr?.toIntOrNull() ?: 0
                    val m = minStr?.toIntOrNull() ?: 0
                    if (amPm.equals("pm", ignoreCase = true) && h < 12) h += 12
                    if (amPm.equals("am", ignoreCase = true) && h == 12) h = 0
                    hour = h
                    minute = m
                }
                h24Str != null -> {
                    hour = h24Str.toIntOrNull() ?: 0
                    minute = m24Str?.toIntOrNull() ?: 0
                }
            }

            dueTimeMillis = (hour * 3600L + minute * 60L) * 1000L
            isAllDay = false
            text = timeMatcher.replaceFirst("").trim()
        }

        // 5. Parse Due Date
        val baseCalendar = Calendar.getInstance().apply {
            timeInMillis = nowMillis
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        var dueDateMillis: Long? = null

        // Check relative days: today, tomorrow, tmrw, in N days
        val relMatcher = RELATIVE_DAYS_REGEX.matcher(text)
        if (relMatcher.find()) {
            val relToken = relMatcher.group(1).lowercase(Locale.ROOT)
            val nDaysStr = relMatcher.group(2)

            when {
                relToken.startsWith("today") || relToken.startsWith("tonight") -> {
                    dueDateMillis = baseCalendar.timeInMillis
                }
                relToken.startsWith("tomorrow") || relToken.startsWith("tmrw") -> {
                    baseCalendar.add(Calendar.DAY_OF_YEAR, 1)
                    dueDateMillis = baseCalendar.timeInMillis
                }
                nDaysStr != null -> {
                    val days = nDaysStr.toIntOrNull() ?: 0
                    baseCalendar.add(Calendar.DAY_OF_YEAR, days)
                    dueDateMillis = baseCalendar.timeInMillis
                }
            }
            text = relMatcher.replaceFirst("").trim()
        }

        // Check day of week: monday, next friday, etc.
        if (dueDateMillis == null) {
            val dowMatcher = DAY_OF_WEEK_REGEX.matcher(text)
            if (dowMatcher.find()) {
                val dowStr = dowMatcher.group(1).lowercase(Locale.ROOT)
                val targetDayOfWeek = when (dowStr) {
                    "sunday", "sun" -> Calendar.SUNDAY
                    "monday", "mon" -> Calendar.MONDAY
                    "tuesday", "tue" -> Calendar.TUESDAY
                    "wednesday", "wed" -> Calendar.WEDNESDAY
                    "thursday", "thu" -> Calendar.THURSDAY
                    "friday", "fri" -> Calendar.FRIDAY
                    "saturday", "sat" -> Calendar.SATURDAY
                    else -> -1
                }

                if (targetDayOfWeek != -1) {
                    val currentDayOfWeek = baseCalendar.get(Calendar.DAY_OF_WEEK)
                    var daysUntil = targetDayOfWeek - currentDayOfWeek
                    if (daysUntil <= 0) {
                        daysUntil += 7 // Move to next week
                    }
                    baseCalendar.add(Calendar.DAY_OF_YEAR, daysUntil)
                    dueDateMillis = baseCalendar.timeInMillis
                    text = dowMatcher.replaceFirst("").trim()
                }
            }
        }

        // Check Month Date: oct 15, december 25th
        if (dueDateMillis == null) {
            val monthMatcher = MONTH_DATE_REGEX.matcher(text)
            if (monthMatcher.find()) {
                val monthStr = monthMatcher.group(1).lowercase(Locale.ROOT)
                val dayNum = monthMatcher.group(2).toIntOrNull() ?: 1
                val targetMonth = parseMonth(monthStr)

                if (targetMonth != -1) {
                    baseCalendar.set(Calendar.MONTH, targetMonth)
                    baseCalendar.set(Calendar.DAY_OF_MONTH, dayNum)
                    // If target date in current year has already passed, advance to next year
                    if (baseCalendar.timeInMillis < nowMillis - (24 * 60 * 60 * 1000L)) {
                        baseCalendar.add(Calendar.YEAR, 1)
                    }
                    dueDateMillis = baseCalendar.timeInMillis
                    text = monthMatcher.replaceFirst("").trim()
                }
            }
        }

        // If time was specified but no date was parsed, default to today
        if (dueTimeMillis != null && dueDateMillis == null) {
            dueDateMillis = baseCalendar.timeInMillis
        }

        // Clean up multi-whitespace in title
        val cleanedTitle = text.replace(Regex("\\s+"), " ").trim()

        return ParsedTaskResult(
            title = cleanedTitle,
            dueDate = dueDateMillis,
            dueTime = dueTimeMillis,
            isAllDay = isAllDay,
            priority = priority,
            tags = tags,
            recurrenceRule = recurrenceRule
        )
    }

    private fun parseMonth(m: String): Int {
        return when {
            m.startsWith("jan") -> Calendar.JANUARY
            m.startsWith("feb") -> Calendar.FEBRUARY
            m.startsWith("mar") -> Calendar.MARCH
            m.startsWith("apr") -> Calendar.APRIL
            m.startsWith("may") -> Calendar.MAY
            m.startsWith("jun") -> Calendar.JUNE
            m.startsWith("jul") -> Calendar.JULY
            m.startsWith("aug") -> Calendar.AUGUST
            m.startsWith("sep") -> Calendar.SEPTEMBER
            m.startsWith("oct") -> Calendar.OCTOBER
            m.startsWith("nov") -> Calendar.NOVEMBER
            m.startsWith("dec") -> Calendar.DECEMBER
            else -> -1
        }
    }
}
