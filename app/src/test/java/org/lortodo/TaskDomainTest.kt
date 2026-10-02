package org.lortodo

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.lortodo.domain.model.Priority
import org.lortodo.domain.model.Subtask
import org.lortodo.domain.model.Tag
import org.lortodo.domain.model.Task
import java.util.Calendar

class TaskDomainTest {

    @Test
    fun testSubtaskProgressCalculation() {
        val task = Task(
            title = "Project milestone",
            subtasks = listOf(
                Subtask(id = 1, taskId = 1, title = "Step 1", isDone = true),
                Subtask(id = 2, taskId = 1, title = "Step 2", isDone = true),
                Subtask(id = 3, taskId = 1, title = "Step 3", isDone = false),
                Subtask(id = 4, taskId = 1, title = "Step 4", isDone = false),
                Subtask(id = 5, taskId = 1, title = "Step 5", isDone = false)
            )
        )

        assertEquals(5, task.subtasksTotalCount)
        assertEquals(2, task.subtasksCompletedCount)
    }

    @Test
    fun testPriorityFromLevel() {
        assertEquals(Priority.NONE, Priority.fromLevel(0))
        assertEquals(Priority.LOW, Priority.fromLevel(1))
        assertEquals(Priority.MEDIUM, Priority.fromLevel(2))
        assertEquals(Priority.HIGH, Priority.fromLevel(3))
        assertEquals(Priority.NONE, Priority.fromLevel(99)) // Fallback
    }

    @Test
    fun testOverdueStatus() {
        val yesterdayMillis = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -1)
        }.timeInMillis

        val tomorrowMillis = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, 1)
        }.timeInMillis

        val overdueTask = Task(
            title = "Missed deadline",
            dueDate = yesterdayMillis,
            isCompleted = false
        )
        assertTrue(overdueTask.isOverdue)

        val completedOverdueTask = Task(
            title = "Done late",
            dueDate = yesterdayMillis,
            isCompleted = true
        )
        assertFalse(completedOverdueTask.isOverdue)

        val futureTask = Task(
            title = "Upcoming task",
            dueDate = tomorrowMillis,
            isCompleted = false
        )
        assertFalse(futureTask.isOverdue)
    }

    @Test
    fun testTaskTagAssociation() {
        val tag1 = Tag(id = 10, name = "work", colorHex = "#1E88E5")
        val tag2 = Tag(id = 11, name = "urgent", colorHex = "#E53935")

        val task = Task(
            title = "Critical report",
            tags = listOf(tag1, tag2)
        )

        assertEquals(2, task.tags.size)
        assertTrue(task.tags.any { it.name == "work" })
        assertTrue(task.tags.any { it.name == "urgent" })
    }
}
