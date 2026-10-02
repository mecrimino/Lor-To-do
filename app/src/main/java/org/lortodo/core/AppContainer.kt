package org.lortodo.core

import android.content.Context
import org.lortodo.data.local.AppDatabase
import org.lortodo.data.repository.FocusRepositoryImpl
import org.lortodo.data.repository.ReminderRepositoryImpl
import org.lortodo.data.repository.SavedFilterRepositoryImpl
import org.lortodo.data.repository.TagRepositoryImpl
import org.lortodo.data.repository.TaskListRepositoryImpl
import org.lortodo.data.repository.TaskRepositoryImpl
import org.lortodo.data.repository.TemplateRepositoryImpl
import org.lortodo.data.repository.UserPreferencesRepositoryImpl
import org.lortodo.domain.repository.FocusRepository
import org.lortodo.domain.repository.ReminderRepository
import org.lortodo.domain.repository.SavedFilterRepository
import org.lortodo.domain.repository.TagRepository
import org.lortodo.domain.repository.TaskListRepository
import org.lortodo.domain.repository.TaskRepository
import org.lortodo.domain.repository.TemplateRepository
import org.lortodo.domain.repository.UserPreferencesRepository
import org.lortodo.reminders.AlarmScheduler

interface AppContainer {
    val database: AppDatabase
    val taskRepository: TaskRepository
    val taskListRepository: TaskListRepository
    val tagRepository: TagRepository
    val reminderRepository: ReminderRepository
    val focusRepository: FocusRepository
    val templateRepository: TemplateRepository
    val savedFilterRepository: SavedFilterRepository
    val userPreferencesRepository: UserPreferencesRepository
    val alarmScheduler: AlarmScheduler
}

class DefaultAppContainer(private val context: Context) : AppContainer {

    override val database: AppDatabase by lazy {
        AppDatabase.getInstance(context)
    }

    override val alarmScheduler: AlarmScheduler by lazy {
        AlarmScheduler(context)
    }

    override val taskRepository: TaskRepository by lazy {
        TaskRepositoryImpl(
            taskDao = database.taskDao(),
            subtaskDao = database.subtaskDao(),
            taskListDao = database.taskListDao(),
            tagDao = database.tagDao(),
            reminderDao = database.reminderDao()
        )
    }

    override val taskListRepository: TaskListRepository by lazy {
        TaskListRepositoryImpl(
            taskListDao = database.taskListDao(),
            taskDao = database.taskDao()
        )
    }

    override val tagRepository: TagRepository by lazy {
        TagRepositoryImpl(tagDao = database.tagDao())
    }

    override val reminderRepository: ReminderRepository by lazy {
        ReminderRepositoryImpl(reminderDao = database.reminderDao())
    }

    override val focusRepository: FocusRepository by lazy {
        FocusRepositoryImpl(
            focusSessionDao = database.focusSessionDao(),
            taskDao = database.taskDao()
        )
    }

    override val templateRepository: TemplateRepository by lazy {
        TemplateRepositoryImpl(templateDao = database.templateDao())
    }

    override val savedFilterRepository: SavedFilterRepository by lazy {
        SavedFilterRepositoryImpl(savedFilterDao = database.savedFilterDao())
    }

    override val userPreferencesRepository: UserPreferencesRepository by lazy {
        UserPreferencesRepositoryImpl(context)
    }
}
