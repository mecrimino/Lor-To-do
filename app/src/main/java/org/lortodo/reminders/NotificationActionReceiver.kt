package org.lortodo.reminders

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.lortodo.LorTodoApp
import org.lortodo.domain.engine.RecurrenceEngine
import org.lortodo.domain.model.Reminder

class NotificationActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val taskId = intent.getLongExtra(NotificationHelper.EXTRA_TASK_ID, -1L)
        val reminderId = intent.getLongExtra(NotificationHelper.EXTRA_REMINDER_ID, -1L)
        if (taskId == -1L) return

        val pendingResult = goAsync()
        val app = context.applicationContext as LorTodoApp
        val container = app.appContainer
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Cancel the notification
        notificationManager.cancel(taskId.toInt())

        when (intent.action) {
            NotificationHelper.ACTION_MARK_DONE -> {
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val task = container.taskRepository.getTaskByIdOnce(taskId)
                        if (task != null) {
                            container.taskRepository.setTaskCompleted(taskId, true)

                            // If recurring, schedule next occurrence
                            val nextTask = RecurrenceEngine.createNextOccurrence(task)
                            if (nextTask != null) {
                                val tagIds = task.tags.map { it.id }
                                val newTaskId = container.taskRepository.insertTask(nextTask, tagIds)
                                // Reschedule reminders for next task if any
                                task.reminders.forEach { origReminder ->
                                    if (nextTask.dueDate != null) {
                                        val newTrigger = nextTask.dueDate + (nextTask.dueTime ?: 0L) - (origReminder.offsetMinutes * 60 * 1000L)
                                        val newReminder = Reminder(
                                            taskId = newTaskId,
                                            triggerAtMillis = newTrigger,
                                            offsetMinutes = origReminder.offsetMinutes,
                                            type = origReminder.type
                                        )
                                        val newReminderId = container.reminderRepository.insertReminder(newReminder)
                                        container.alarmScheduler.scheduleReminder(
                                            newReminder.copy(id = newReminderId),
                                            nextTask.title,
                                            nextTask.notes
                                        )
                                    }
                                }
                            }
                        }
                    } finally {
                        pendingResult.finish()
                    }
                }
            }

            NotificationHelper.ACTION_SNOOZE -> {
                val snoozeMinutes = intent.getIntExtra(NotificationHelper.EXTRA_SNOOZE_MINUTES, 10)
                val newTriggerTime = System.currentTimeMillis() + (snoozeMinutes * 60 * 1000L)

                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val task = container.taskRepository.getTaskByIdOnce(taskId)
                        if (task != null) {
                            val snoozeReminder = Reminder(
                                taskId = taskId,
                                triggerAtMillis = newTriggerTime,
                                offsetMinutes = 0
                            )
                            val newId = container.reminderRepository.insertReminder(snoozeReminder)
                            container.alarmScheduler.scheduleReminder(
                                snoozeReminder.copy(id = newId),
                                task.title,
                                task.notes
                            )
                        }
                    } finally {
                        pendingResult.finish()
                    }
                }
            }

            else -> pendingResult.finish()
        }
    }
}
