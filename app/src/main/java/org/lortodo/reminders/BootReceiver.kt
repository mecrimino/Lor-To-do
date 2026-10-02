package org.lortodo.reminders

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.lortodo.LorTodoApp

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == Intent.ACTION_MY_PACKAGE_REPLACED ||
            intent.action == "android.intent.action.QUICKBOOT_POWERON" ||
            intent.action == "com.htc.intent.action.QUICKBOOT_POWERON"
        ) {
            val pendingResult = goAsync()
            val app = context.applicationContext as LorTodoApp
            val container = app.appContainer

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val activeReminders = container.reminderRepository.getAllActiveReminders().first()
                    for (reminder in activeReminders) {
                        val task = container.taskRepository.getTaskByIdOnce(reminder.taskId)
                        if (task != null && !task.isCompleted && !task.isDeleted) {
                            container.alarmScheduler.scheduleReminder(reminder, task.title, task.notes)
                        }
                    }
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
