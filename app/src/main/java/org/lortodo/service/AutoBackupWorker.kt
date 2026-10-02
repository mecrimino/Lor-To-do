package org.lortodo.service

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.flow.first
import org.lortodo.LorTodoApp
import org.lortodo.data.backup.BackupManager
import org.lortodo.domain.repository.SmartView
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AutoBackupWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val app = applicationContext as LorTodoApp
            val container = app.appContainer

            val tasks = container.taskRepository.getTasksBySmartView(SmartView.ALL).first()
            val lists = container.taskListRepository.getAllLists().first()
            val tags = container.tagRepository.getAllTags().first()
            val prefs = container.userPreferencesRepository.userPreferences.first()

            val jsonContent = BackupManager.exportToJson(tasks, lists, tags, prefs)

            val backupDir = File(applicationContext.filesDir, "backups").apply {
                if (!exists()) mkdirs()
            }

            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            val backupFile = File(backupDir, "lor_backup_$timestamp.json")
            backupFile.writeText(jsonContent)

            // Keep only the last 7 auto-backups (keep last N)
            val files = backupDir.listFiles { file -> file.name.startsWith("lor_backup_") && file.name.endsWith(".json") }
            if (files != null && files.size > 7) {
                files.sortBy { it.lastModified() }
                val toDelete = files.size - 7
                for (i in 0 until toDelete) {
                    files[i].delete()
                }
            }

            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
