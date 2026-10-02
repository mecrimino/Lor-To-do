package org.lortodo

import android.app.Application
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import org.lortodo.core.AppContainer
import org.lortodo.core.DefaultAppContainer
import org.lortodo.reminders.NotificationHelper
import org.lortodo.service.AutoBackupWorker
import org.lortodo.service.TrashPurgeWorker
import java.util.concurrent.TimeUnit

class LorTodoApp : Application() {

    lateinit var appContainer: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        appContainer = DefaultAppContainer(this)

        // Initialize Notification Channels
        NotificationHelper.createNotificationChannels(this)

        // Schedule periodic 30-day trash housekeeping worker
        val purgeRequest = PeriodicWorkRequestBuilder<TrashPurgeWorker>(24, TimeUnit.HOURS)
            .build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "lor_trash_purge_worker",
            ExistingPeriodicWorkPolicy.KEEP,
            purgeRequest
        )

        // Schedule periodic auto-backup worker (daily, keeps last N)
        val backupRequest = PeriodicWorkRequestBuilder<AutoBackupWorker>(24, TimeUnit.HOURS)
            .build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "lor_auto_backup_worker",
            ExistingPeriodicWorkPolicy.KEEP,
            backupRequest
        )
    }
}
