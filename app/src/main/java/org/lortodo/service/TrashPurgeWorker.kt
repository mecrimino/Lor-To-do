package org.lortodo.service

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import org.lortodo.LorTodoApp

class TrashPurgeWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val app = applicationContext as LorTodoApp
        val container = app.appContainer

        val thirtyDaysAgoMillis = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000L)
        container.taskRepository.purgeDeletedTasksOlderThan(thirtyDaysAgoMillis)

        return Result.success()
    }
}
