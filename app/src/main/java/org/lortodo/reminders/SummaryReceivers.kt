package org.lortodo.reminders

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.lortodo.LorTodoApp
import org.lortodo.domain.repository.SmartView

class DailySummaryReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        val app = context.applicationContext as LorTodoApp
        val container = app.appContainer

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val prefs = container.userPreferencesRepository.userPreferences.first()
                if (prefs.dailySummaryEnabled) {
                    val todayTasks = container.taskRepository.getTasksBySmartView(SmartView.TODAY).first()
                    val pendingToday = todayTasks.filter { !it.isCompleted }
                    if (pendingToday.isNotEmpty()) {
                        NotificationHelper.showDailySummaryNotification(
                            context = context,
                            taskCount = pendingToday.size,
                            firstTaskTitle = pendingToday.firstOrNull()?.title
                        )
                    }
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}

class OverdueNudgeReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        val app = context.applicationContext as LorTodoApp
        val container = app.appContainer

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val prefs = container.userPreferencesRepository.userPreferences.first()
                if (prefs.overdueNudgesEnabled) {
                    val overdueTasks = container.taskRepository.getTasksBySmartView(SmartView.OVERDUE).first()
                    if (overdueTasks.isNotEmpty()) {
                        NotificationHelper.showDailySummaryNotification(
                            context = context,
                            taskCount = overdueTasks.size,
                            firstTaskTitle = "Overdue: ${overdueTasks.first().title}"
                        )
                    }
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
