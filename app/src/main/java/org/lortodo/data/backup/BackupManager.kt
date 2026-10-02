package org.lortodo.data.backup

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.lortodo.domain.model.Priority
import org.lortodo.domain.model.Subtask
import org.lortodo.domain.model.Tag
import org.lortodo.domain.model.Task
import org.lortodo.domain.model.TaskList
import org.lortodo.domain.model.UserPreferences
import java.io.BufferedReader
import java.io.StringReader
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object BackupManager {

    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    fun exportToJson(
        tasks: List<Task>,
        lists: List<TaskList>,
        tags: List<Tag>,
        preferences: UserPreferences? = null
    ): String {
        val backupTasks = tasks.map { t ->
            BackupTask(
                id = t.id,
                title = t.title,
                notes = t.notes,
                listId = t.listId,
                priority = t.priority,
                isCompleted = t.isCompleted,
                completedAt = t.completedAt,
                dueDate = t.dueDate,
                dueTime = t.dueTime,
                isAllDay = t.isAllDay,
                isStarred = t.isStarred,
                recurrenceRule = t.recurrenceRule,
                repeatFrom = t.repeatFrom,
                estimateMinutes = t.estimateMinutes,
                sortOrder = t.sortOrder,
                createdAt = t.createdAt,
                updatedAt = t.updatedAt,
                subtasks = t.subtasks.map { s -> BackupSubtask(s.title, s.isDone, s.sortOrder) }
            )
        }

        val backupLists = lists.map { l ->
            BackupTaskList(
                id = l.id,
                name = l.name,
                colorHex = l.colorHex,
                icon = l.icon,
                sortOrder = l.sortOrder,
                isArchived = l.isArchived
            )
        }

        val backupTags = tags.map { tag ->
            BackupTag(id = tag.id, name = tag.name, colorHex = tag.colorHex)
        }

        val taskTagRefs = tasks.flatMap { t ->
            t.tags.map { tag -> BackupTaskTagRef(taskId = t.id, tagId = tag.id) }
        }

        val payload = BackupPayload(
            tasks = backupTasks,
            lists = backupLists,
            tags = backupTags,
            taskTagRefs = taskTagRefs,
            preferences = preferences
        )

        return json.encodeToString(payload)
    }

    fun parseBackupJson(jsonString: String): BackupPayload? {
        return try {
            json.decodeFromString<BackupPayload>(jsonString)
        } catch (e: Exception) {
            null
        }
    }

    fun exportToCsv(tasks: List<Task>): String {
        val sb = StringBuilder()
        sb.append("Title,Notes,Priority,DueDate,IsCompleted,Subtasks,Tags\n")

        for (task in tasks) {
            val titleEscaped = escapeCsv(task.title)
            val notesEscaped = escapeCsv(task.notes)
            val priority = task.priority.name
            val dueDateStr = task.dueDate?.let { dateFormat.format(Date(it)) } ?: ""
            val isCompleted = task.isCompleted.toString()
            val subtasksStr = escapeCsv(task.subtasks.joinToString("; ") { "${if (it.isDone) "[x]" else "[ ]"} ${it.title}" })
            val tagsStr = escapeCsv(task.tags.joinToString("; ") { it.name })

            sb.append("$titleEscaped,$notesEscaped,$priority,$dueDateStr,$isCompleted,$subtasksStr,$tagsStr\n")
        }

        return sb.toString()
    }

    fun importFromCsv(csvContent: String, defaultListId: Long = 1L): List<Task> {
        val tasks = mutableListOf<Task>()
        val reader = BufferedReader(StringReader(csvContent))
        val header = reader.readLine() ?: return emptyList()

        val isTodoist = header.contains("CONTENT", ignoreCase = true)

        var line = reader.readLine()
        while (line != null) {
            if (line.isNotBlank()) {
                val tokens = parseCsvLine(line)
                if (isTodoist && tokens.isNotEmpty()) {
                    // Todoist format: CONTENT,PRIORITY,DATE,etc.
                    val title = tokens.getOrNull(0) ?: ""
                    val priority = Priority.fromString(tokens.getOrNull(1))
                    val dateStr = tokens.getOrNull(2)
                    val dateMillis = dateStr?.let { parseDateSafe(it) }

                    if (title.isNotBlank()) {
                        tasks.add(
                            Task(
                                title = title,
                                priority = priority,
                                dueDate = dateMillis,
                                listId = defaultListId
                            )
                        )
                    }
                } else if (tokens.isNotEmpty()) {
                    // Standard LOR CSV format
                    val title = tokens.getOrNull(0) ?: ""
                    val notes = tokens.getOrNull(1) ?: ""
                    val priority = Priority.fromString(tokens.getOrNull(2))
                    val dateMillis = tokens.getOrNull(3)?.let { parseDateSafe(it) }
                    val isCompleted = tokens.getOrNull(4)?.toBooleanStrictOrNull() ?: false

                    val subtasksStr = tokens.getOrNull(5) ?: ""
                    val subtasks = if (subtasksStr.isNotBlank()) {
                        subtasksStr.split(";").mapIndexed { index, subStr ->
                            val trimmed = subStr.trim()
                            val done = trimmed.startsWith("[x]")
                            val subTitle = trimmed.removePrefix("[x]").removePrefix("[ ]").trim()
                            Subtask(taskId = 0, title = subTitle, isDone = done, sortOrder = index)
                        }
                    } else emptyList()

                    val tagsStr = tokens.getOrNull(6) ?: ""
                    val tags = if (tagsStr.isNotBlank()) {
                        tagsStr.split(";").map { Tag(name = it.trim()) }
                    } else emptyList()

                    if (title.isNotBlank()) {
                        tasks.add(
                            Task(
                                title = title,
                                notes = notes,
                                priority = priority,
                                dueDate = dateMillis,
                                isCompleted = isCompleted,
                                listId = defaultListId,
                                subtasks = subtasks,
                                tags = tags
                            )
                        )
                    }
                }
            }
            line = reader.readLine()
        }

        return tasks
    }

    fun exportToMarkdown(tasks: List<Task>, listName: String = "Tasks"): String {
        val sb = StringBuilder()
        sb.append("# $listName\n\n")

        for (task in tasks) {
            val check = if (task.isCompleted) "[x]" else "[ ]"
            sb.append("- $check ${task.title}")
            if (task.dueDate != null) {
                sb.append(" 📅 ${dateFormat.format(Date(task.dueDate))}")
            }
            if (task.priority != Priority.NONE) {
                sb.append(" !${task.priority.name.lowercase()}")
            }
            if (task.tags.isNotEmpty()) {
                sb.append(" " + task.tags.joinToString(" ") { "#${it.name}" })
            }
            sb.append("\n")

            if (task.notes.isNotBlank()) {
                task.notes.lines().forEach { noteLine ->
                    sb.append("  > $noteLine\n")
                }
            }

            for (subtask in task.subtasks) {
                val subCheck = if (subtask.isDone) "[x]" else "[ ]"
                sb.append("  - $subCheck ${subtask.title}\n")
            }
        }

        return sb.toString()
    }

    fun importFromPlainText(plainText: String, defaultListId: Long = 1L): List<Task> {
        val tasks = mutableListOf<Task>()
        for (line in plainText.lines()) {
            val trimmed = line.trim()
            if (trimmed.isNotBlank()) {
                val isDone = trimmed.startsWith("- [x]") || trimmed.startsWith("[x]")
                val cleanTitle = trimmed
                    .removePrefix("- [x]")
                    .removePrefix("- [ ]")
                    .removePrefix("[x]")
                    .removePrefix("[ ]")
                    .removePrefix("-")
                    .removePrefix("*")
                    .trim()

                if (cleanTitle.isNotBlank()) {
                    tasks.add(
                        Task(
                            title = cleanTitle,
                            isCompleted = isDone,
                            listId = defaultListId
                        )
                    )
                }
            }
        }
        return tasks
    }

    private fun escapeCsv(str: String): String {
        return if (str.contains(",") || str.contains("\"") || str.contains("\n") || str.contains(";")) {
            "\"" + str.replace("\"", "\"\"") + "\""
        } else {
            str
        }
    }

    private fun parseCsvLine(line: String): List<String> {
        val tokens = mutableListOf<String>()
        val sb = StringBuilder()
        var insideQuotes = false

        var i = 0
        while (i < line.length) {
            val c = line[i]
            when {
                c == '\"' -> {
                    if (insideQuotes && i + 1 < line.length && line[i + 1] == '\"') {
                        sb.append('\"')
                        i++ // Skip escaped quote
                    } else {
                        insideQuotes = !insideQuotes
                    }
                }
                c == ',' && !insideQuotes -> {
                    tokens.add(sb.toString().trim())
                    sb.clear()
                }
                else -> sb.append(c)
            }
            i++
        }
        tokens.add(sb.toString().trim())
        return tokens
    }

    private fun parseDateSafe(str: String): Long? {
        return try {
            dateFormat.parse(str.trim())?.time
        } catch (e: Exception) {
            null
        }
    }
}
