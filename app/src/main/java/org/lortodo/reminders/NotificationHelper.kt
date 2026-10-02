package org.lortodo.reminders

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import org.lortodo.MainActivity
import org.lortodo.R

object NotificationHelper {

    const val CHANNEL_REMINDERS = "lor_reminders_channel"
    const val CHANNEL_SUMMARY = "lor_summary_channel"
    const val CHANNEL_OVERDUE = "lor_overdue_channel"

    const val ACTION_MARK_DONE = "org.lortodo.ACTION_MARK_DONE"
    const val ACTION_SNOOZE = "org.lortodo.ACTION_SNOOZE"
    const val EXTRA_TASK_ID = "extra_task_id"
    const val EXTRA_REMINDER_ID = "extra_reminder_id"
    const val EXTRA_SNOOZE_MINUTES = "extra_snooze_minutes"

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Reminders Channel
            val reminderChannel = NotificationChannel(
                CHANNEL_REMINDERS,
                context.getString(R.string.channel_reminders_name),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = context.getString(R.string.channel_reminders_description)
                enableVibration(true)
                enableLights(true)
            }

            // Daily Summary Channel
            val summaryChannel = NotificationChannel(
                CHANNEL_SUMMARY,
                context.getString(R.string.channel_summary_name),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = context.getString(R.string.channel_summary_description)
            }

            // Overdue Nudges Channel
            val overdueChannel = NotificationChannel(
                CHANNEL_OVERDUE,
                context.getString(R.string.channel_overdue_name),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = context.getString(R.string.channel_overdue_description)
            }

            notificationManager.createNotificationChannels(listOf(reminderChannel, summaryChannel, overdueChannel))
        }
    }

    fun showTaskReminderNotification(
        context: Context,
        taskId: Long,
        reminderId: Long,
        title: String,
        notes: String,
        hideSensitive: Boolean = false
    ) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val openIntent = Intent(Intent.ACTION_VIEW, Uri.parse("lortodo://task/$taskId"), context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openPendingIntent = PendingIntent.getActivity(
            context,
            taskId.toInt(),
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Done Action
        val doneIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = ACTION_MARK_DONE
            putExtra(EXTRA_TASK_ID, taskId)
            putExtra(EXTRA_REMINDER_ID, reminderId)
        }
        val donePendingIntent = PendingIntent.getBroadcast(
            context,
            (taskId * 100 + 1).toInt(),
            doneIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Snooze 10m Action
        val snooze10Intent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = ACTION_SNOOZE
            putExtra(EXTRA_TASK_ID, taskId)
            putExtra(EXTRA_REMINDER_ID, reminderId)
            putExtra(EXTRA_SNOOZE_MINUTES, 10)
        }
        val snooze10PendingIntent = PendingIntent.getBroadcast(
            context,
            (taskId * 100 + 2).toInt(),
            snooze10Intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Snooze 1h Action
        val snooze60Intent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = ACTION_SNOOZE
            putExtra(EXTRA_TASK_ID, taskId)
            putExtra(EXTRA_REMINDER_ID, reminderId)
            putExtra(EXTRA_SNOOZE_MINUTES, 60)
        }
        val snooze60PendingIntent = PendingIntent.getBroadcast(
            context,
            (taskId * 100 + 3).toInt(),
            snooze60Intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val displayTitle = if (hideSensitive) "Task reminder" else title
        val displayText = if (hideSensitive) "You have a task due" else notes.ifBlank { "Task is due now" }

        val builder = NotificationCompat.Builder(context, CHANNEL_REMINDERS)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(displayTitle)
            .setContentText(displayText)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setContentIntent(openPendingIntent)
            .addAction(R.drawable.ic_notification, context.getString(R.string.action_done), donePendingIntent)
            .addAction(R.drawable.ic_notification, context.getString(R.string.action_snooze_10m), snooze10PendingIntent)
            .addAction(R.drawable.ic_notification, context.getString(R.string.action_snooze_1h), snooze60PendingIntent)

        notificationManager.notify(taskId.toInt(), builder.build())
    }

    fun showDailySummaryNotification(context: Context, taskCount: Int, firstTaskTitle: String?) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val openIntent = Intent(context, MainActivity::class.java)
        val openPendingIntent = PendingIntent.getActivity(
            context,
            9999,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val content = if (taskCount == 1 && firstTaskTitle != null) {
            "1 task scheduled for today: $firstTaskTitle"
        } else {
            "You have $taskCount tasks scheduled for today."
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_SUMMARY)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Today's Agenda")
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(openPendingIntent)

        notificationManager.notify(9999, builder.build())
    }
}
