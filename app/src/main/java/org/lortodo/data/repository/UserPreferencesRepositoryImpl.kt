package org.lortodo.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.lortodo.domain.model.AccentColor
import org.lortodo.domain.model.AppTheme
import org.lortodo.domain.model.CompletedTaskBehavior
import org.lortodo.domain.model.DateFormatPreference
import org.lortodo.domain.model.FirstDayOfWeek
import org.lortodo.domain.model.Priority
import org.lortodo.domain.model.SwipeAction
import org.lortodo.domain.model.TimeFormatPreference
import org.lortodo.domain.model.UserPreferences
import org.lortodo.domain.model.ViewDensity
import org.lortodo.domain.repository.UserPreferencesRepository

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_settings")

class UserPreferencesRepositoryImpl(
    private val context: Context
) : UserPreferencesRepository {

    private object PreferencesKeys {
        val USER_NAME = stringPreferencesKey("user_name")
        val THEME = stringPreferencesKey("app_theme")
        val ACCENT_COLOR = stringPreferencesKey("accent_color")
        val DENSITY = stringPreferencesKey("view_density")
        val FIRST_DAY_OF_WEEK = stringPreferencesKey("first_day_of_week")
        val TIME_FORMAT = stringPreferencesKey("time_format")
        val DATE_FORMAT = stringPreferencesKey("date_format")
        val DEFAULT_LIST_ID = longPreferencesKey("default_list_id")
        val DEFAULT_PRIORITY = intPreferencesKey("default_priority")
        val SWIPE_RIGHT_ACTION = stringPreferencesKey("swipe_right_action")
        val SWIPE_LEFT_ACTION = stringPreferencesKey("swipe_left_action")
        val COMPLETED_TASK_BEHAVIOR = stringPreferencesKey("completed_task_behavior")
        val HAPTICS_ENABLED = booleanPreferencesKey("haptics_enabled")
        val SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
        val APP_LOCK_ENABLED = booleanPreferencesKey("app_lock_enabled")
        val AUTO_LOCK_TIMEOUT = intPreferencesKey("auto_lock_timeout")
        val HIDE_SENSITIVE_NOTIFICATIONS = booleanPreferencesKey("hide_sensitive_notifications")
        val HIDE_IN_RECENTS = booleanPreferencesKey("hide_in_recents")
        val DAILY_SUMMARY_ENABLED = booleanPreferencesKey("daily_summary_enabled")
        val DAILY_SUMMARY_TIME = longPreferencesKey("daily_summary_time")
        val OVERDUE_NUDGES_ENABLED = booleanPreferencesKey("overdue_nudges_enabled")
        val POMODORO_WORK_DURATION = intPreferencesKey("pomodoro_work_duration")
        val POMODORO_BREAK_DURATION = intPreferencesKey("pomodoro_break_duration")
    }

    override val userPreferences: Flow<UserPreferences> = context.dataStore.data.map { prefs ->
        UserPreferences(
            userName = prefs[PreferencesKeys.USER_NAME] ?: "Ene",
            theme = prefs[PreferencesKeys.THEME]?.let { runCatching { AppTheme.valueOf(it) }.getOrNull() } ?: AppTheme.SYSTEM,
            accentColor = prefs[PreferencesKeys.ACCENT_COLOR]?.let { runCatching { AccentColor.valueOf(it) }.getOrNull() } ?: AccentColor.YELLOW,
            density = prefs[PreferencesKeys.DENSITY]?.let { runCatching { ViewDensity.valueOf(it) }.getOrNull() } ?: ViewDensity.COMFORTABLE,
            firstDayOfWeek = prefs[PreferencesKeys.FIRST_DAY_OF_WEEK]?.let { runCatching { FirstDayOfWeek.valueOf(it) }.getOrNull() } ?: FirstDayOfWeek.MONDAY,
            timeFormat = prefs[PreferencesKeys.TIME_FORMAT]?.let { runCatching { TimeFormatPreference.valueOf(it) }.getOrNull() } ?: TimeFormatPreference.SYSTEM,
            dateFormat = prefs[PreferencesKeys.DATE_FORMAT]?.let { runCatching { DateFormatPreference.valueOf(it) }.getOrNull() } ?: DateFormatPreference.SYSTEM,
            defaultListId = prefs[PreferencesKeys.DEFAULT_LIST_ID] ?: 1L,
            defaultPriority = Priority.fromLevel(prefs[PreferencesKeys.DEFAULT_PRIORITY] ?: 0),
            swipeRightAction = prefs[PreferencesKeys.SWIPE_RIGHT_ACTION]?.let { runCatching { SwipeAction.valueOf(it) }.getOrNull() } ?: SwipeAction.COMPLETE,
            swipeLeftAction = prefs[PreferencesKeys.SWIPE_LEFT_ACTION]?.let { runCatching { SwipeAction.valueOf(it) }.getOrNull() } ?: SwipeAction.DELETE,
            completedTaskBehavior = prefs[PreferencesKeys.COMPLETED_TASK_BEHAVIOR]?.let { runCatching { CompletedTaskBehavior.valueOf(it) }.getOrNull() } ?: CompletedTaskBehavior.SHOW_AT_BOTTOM,
            hapticsEnabled = prefs[PreferencesKeys.HAPTICS_ENABLED] ?: true,
            soundEnabled = prefs[PreferencesKeys.SOUND_ENABLED] ?: true,
            appLockEnabled = prefs[PreferencesKeys.APP_LOCK_ENABLED] ?: false,
            autoLockTimeoutSeconds = prefs[PreferencesKeys.AUTO_LOCK_TIMEOUT] ?: 0,
            hideSensitiveNotifications = prefs[PreferencesKeys.HIDE_SENSITIVE_NOTIFICATIONS] ?: false,
            hideInRecents = prefs[PreferencesKeys.HIDE_IN_RECENTS] ?: false,
            dailySummaryEnabled = prefs[PreferencesKeys.DAILY_SUMMARY_ENABLED] ?: true,
            dailySummaryTimeMillis = prefs[PreferencesKeys.DAILY_SUMMARY_TIME] ?: (9 * 60 * 60 * 1000L),
            overdueNudgesEnabled = prefs[PreferencesKeys.OVERDUE_NUDGES_ENABLED] ?: true,
            pomodoroWorkDurationMin = prefs[PreferencesKeys.POMODORO_WORK_DURATION] ?: 25,
            pomodoroBreakDurationMin = prefs[PreferencesKeys.POMODORO_BREAK_DURATION] ?: 5
        )
    }

    override suspend fun updateUserName(name: String) {
        context.dataStore.edit { it[PreferencesKeys.USER_NAME] = name }
    }

    override suspend fun updateTheme(theme: AppTheme) {
        context.dataStore.edit { it[PreferencesKeys.THEME] = theme.name }
    }

    override suspend fun updateAccentColor(accentColor: AccentColor) {
        context.dataStore.edit { it[PreferencesKeys.ACCENT_COLOR] = accentColor.name }
    }

    override suspend fun updateDensity(density: ViewDensity) {
        context.dataStore.edit { it[PreferencesKeys.DENSITY] = density.name }
    }

    override suspend fun updateFirstDayOfWeek(firstDay: FirstDayOfWeek) {
        context.dataStore.edit { it[PreferencesKeys.FIRST_DAY_OF_WEEK] = firstDay.name }
    }

    override suspend fun updateTimeFormat(timeFormat: TimeFormatPreference) {
        context.dataStore.edit { it[PreferencesKeys.TIME_FORMAT] = timeFormat.name }
    }

    override suspend fun updateDateFormat(dateFormat: DateFormatPreference) {
        context.dataStore.edit { it[PreferencesKeys.DATE_FORMAT] = dateFormat.name }
    }

    override suspend fun updateDefaultListId(listId: Long) {
        context.dataStore.edit { it[PreferencesKeys.DEFAULT_LIST_ID] = listId }
    }

    override suspend fun updateDefaultPriority(priority: Priority) {
        context.dataStore.edit { it[PreferencesKeys.DEFAULT_PRIORITY] = priority.level }
    }

    override suspend fun updateSwipeActions(right: SwipeAction, left: SwipeAction) {
        context.dataStore.edit {
            it[PreferencesKeys.SWIPE_RIGHT_ACTION] = right.name
            it[PreferencesKeys.SWIPE_LEFT_ACTION] = left.name
        }
    }

    override suspend fun updateCompletedTaskBehavior(behavior: CompletedTaskBehavior) {
        context.dataStore.edit { it[PreferencesKeys.COMPLETED_TASK_BEHAVIOR] = behavior.name }
    }

    override suspend fun setHapticsEnabled(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.HAPTICS_ENABLED] = enabled }
    }

    override suspend fun setSoundEnabled(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.SOUND_ENABLED] = enabled }
    }

    override suspend fun setAppLockEnabled(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.APP_LOCK_ENABLED] = enabled }
    }

    override suspend fun setAutoLockTimeout(seconds: Int) {
        context.dataStore.edit { it[PreferencesKeys.AUTO_LOCK_TIMEOUT] = seconds }
    }

    override suspend fun setHideSensitiveNotifications(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.HIDE_SENSITIVE_NOTIFICATIONS] = enabled }
    }

    override suspend fun setHideInRecents(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.HIDE_IN_RECENTS] = enabled }
    }

    override suspend fun setDailySummary(enabled: Boolean, timeMillis: Long) {
        context.dataStore.edit {
            it[PreferencesKeys.DAILY_SUMMARY_ENABLED] = enabled
            it[PreferencesKeys.DAILY_SUMMARY_TIME] = timeMillis
        }
    }

    override suspend fun setOverdueNudges(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.OVERDUE_NUDGES_ENABLED] = enabled }
    }

    override suspend fun updatePomodoroDurations(workMin: Int, breakMin: Int) {
        context.dataStore.edit {
            it[PreferencesKeys.POMODORO_WORK_DURATION] = workMin
            it[PreferencesKeys.POMODORO_BREAK_DURATION] = breakMin
        }
    }
}
