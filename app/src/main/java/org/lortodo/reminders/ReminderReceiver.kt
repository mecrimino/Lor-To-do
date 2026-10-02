package org.lortodo.reminders

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.lortodo.LorTodoApp

class ReminderReceiver : BroadcastReceiver() {

    companion object {
        const val EXTRA_TASK_ID = "extra_task_id"
        const val EXTRA_REMINDER_ID = "extra_reminder_id"
        const val EXTRA_TASK_TITLE = "extra_task_title"
        const val EXTRA_TASK_NOTES = "extra_task_notes"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val taskId = intent.getLongExtra(EXTRA_TASK_ID, -1L)
        val reminderId = intent.getLongExtra(EXTRA_REMINDER_ID, -1L)

        if (taskId == -1L) return

        val pendingResult = goAsync()
        val app = context.applicationContext as LorTodoApp
        val container = app.appContainer

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Check if task is still not completed or not deleted
                val task = container.taskRepository.getTaskByIdOnce(taskId)
                if (task != null && !task.isCompleted && !task.isDeleted) {
                    val prefs = container.userPreferencesRepository.userPreferences.first()
                    NotificationHelper.showTaskReminderNotification(
                        context = context,
                        taskId = taskId,
                        reminderId = reminderId,
                        title = task.title,
                        notes = task.notes,
                        hideSensitive = prefs.hideSensitiveNotifications
                    )
                }

                if (reminderId != -1L) {
                    container.reminderRepository.markReminderFired(reminderId)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
