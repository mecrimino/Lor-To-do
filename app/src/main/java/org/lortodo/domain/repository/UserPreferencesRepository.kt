package org.lortodo.domain.repository

import kotlinx.coroutines.flow.Flow
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

interface UserPreferencesRepository {
    val userPreferences: Flow<UserPreferences>

    suspend fun updateUserName(name: String)
    suspend fun updateTheme(theme: AppTheme)
    suspend fun updateAccentColor(accentColor: AccentColor)
    suspend fun updateDensity(density: ViewDensity)
    suspend fun updateFirstDayOfWeek(firstDay: FirstDayOfWeek)
    suspend fun updateTimeFormat(timeFormat: TimeFormatPreference)
    suspend fun updateDateFormat(dateFormat: DateFormatPreference)
    suspend fun updateDefaultListId(listId: Long)
    suspend fun updateDefaultPriority(priority: Priority)
    suspend fun updateSwipeActions(right: SwipeAction, left: SwipeAction)
    suspend fun updateCompletedTaskBehavior(behavior: CompletedTaskBehavior)
    suspend fun setHapticsEnabled(enabled: Boolean)
    suspend fun setSoundEnabled(enabled: Boolean)
    suspend fun setAppLockEnabled(enabled: Boolean)
    suspend fun setAutoLockTimeout(seconds: Int)
    suspend fun setHideSensitiveNotifications(enabled: Boolean)
    suspend fun setHideInRecents(enabled: Boolean)
    suspend fun setDailySummary(enabled: Boolean, timeMillis: Long)
    suspend fun setOverdueNudges(enabled: Boolean)
    suspend fun updatePomodoroDurations(workMin: Int, breakMin: Int)
}
