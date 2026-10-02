package org.lortodo

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.lortodo.data.backup.BackupManager
import org.lortodo.domain.model.Priority
import org.lortodo.domain.model.Subtask
import org.lortodo.domain.model.Tag
import org.lortodo.domain.model.Task
import org.lortodo.domain.model.TaskList

class BackupManagerTest {

    @Test
    fun testJsonExportAndParseRoundTrip() {
        val lists = listOf(
            TaskList(id = 1, name = "Inbox", colorHex = "#E5A800"),
            TaskList(id = 2, name = "Work", colorHex = "#1E88E5")
        )
        val tags = listOf(
            Tag(id = 1, name = "urgent", colorHex = "#E53935")
        )
        val tasks = listOf(
            Task(
                id = 10,
                title = "Launch product",
                notes = "Checklist and deployment",
                listId = 2,
                priority = Priority.HIGH,
                subtasks = listOf(
                    Subtask(id = 1, taskId = 10, title = "Write docs", isDone = true),
                    Subtask(id = 2, taskId = 10, title = "Tag release", isDone = false)
                ),
                tags = tags
            )
        )

        val jsonString = BackupManager.exportToJson(tasks, lists, tags)
        assertTrue(jsonString.contains("Launch product"))
        assertTrue(jsonString.contains("Write docs"))

        val parsed = BackupManager.parseBackupJson(jsonString)
        assertNotNull(parsed)
        assertEquals(1, parsed?.tasks?.size)
        assertEquals("Launch product", parsed?.tasks?.first()?.title)
        assertEquals(2, parsed?.tasks?.first()?.subtasks?.size)
        assertEquals(2, parsed?.lists?.size)
        assertEquals(1, parsed?.tags?.size)
    }

    @Test
    fun testCsvExportAndImportRoundTrip() {
        val tasks = listOf(
            Task(
                title = "Buy groceries",
                notes = "Apples, Milk",
                priority = Priority.MEDIUM,
                isCompleted = false
            ),
            Task(
                title = "Pay electric bill",
                notes = "",
                priority = Priority.HIGH,
                isCompleted = true
            )
        )

        val csvString = BackupManager.exportToCsv(tasks)
        val imported = BackupManager.importFromCsv(csvString)

        assertEquals(2, imported.size)
        assertEquals("Buy groceries", imported[0].title)
        assertEquals(Priority.MEDIUM, imported[0].priority)
        assertEquals("Pay electric bill", imported[1].title)
        assertEquals(Priority.HIGH, imported[1].priority)
        assertTrue(imported[1].isCompleted)
    }

    @Test
    fun testPlainTextImport() {
        val plainText = """
            - [ ] Buy milk
            - [x] Read news
            * Clean desk
        """.trimIndent()

        val imported = BackupManager.importFromPlainText(plainText)
        assertEquals(3, imported.size)
        assertEquals("Buy milk", imported[0].title)
        assertEquals(false, imported[0].isCompleted)
        assertEquals("Read news", imported[1].title)
        assertEquals(true, imported[1].isCompleted)
        assertEquals("Clean desk", imported[2].title)
    }
}
