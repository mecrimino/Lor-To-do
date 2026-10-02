package org.lortodo.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class AppTheme {
    LIGHT,
    DARK,
    SYSTEM,
    AMOLED
}

@Serializable
enum class AccentColor(val colorHex: String, val displayName: String) {
    YELLOW("#E5A800", "Warm Amber"),
    BLUE("#1E88E5", "Classic Blue"),
    GREEN("#43A047", "Emerald Green"),
    PURPLE("#8E24AA", "Lavender Purple"),
    ORANGE("#FB8C00", "Sunset Orange"),
    DYNAMIC("", "Material You")
}

@Serializable
enum class ViewDensity {
    COMPACT,
    COMFORTABLE
}

@Serializable
enum class FirstDayOfWeek {
    LOCALE,
    SUNDAY,
    MONDAY,
    SATURDAY
}

@Serializable
enum class TimeFormatPreference {
    SYSTEM,
    H12,
    H24
}

@Serializable
enum class DateFormatPreference {
    SYSTEM,
    DD_MM_YYYY,
    MM_DD_YYYY,
    YYYY_MM_DD
}

@Serializable
enum class SwipeAction {
    COMPLETE,
    DELETE,
    SNOOZE,
    NONE
}

@Serializable
enum class CompletedTaskBehavior {
    SHOW_AT_BOTTOM,
    HIDE,
    STRIKETHROUGH_IN_PLACE
}

@Serializable
data class UserPreferences(
    val userName: String = "Ene",
    val theme: AppTheme = AppTheme.SYSTEM,
    val accentColor: AccentColor = AccentColor.YELLOW,
    val density: ViewDensity = ViewDensity.COMFORTABLE,
    val firstDayOfWeek: FirstDayOfWeek = FirstDayOfWeek.MONDAY,
    val timeFormat: TimeFormatPreference = TimeFormatPreference.SYSTEM,
    val dateFormat: DateFormatPreference = DateFormatPreference.SYSTEM,
    val defaultListId: Long = 1L,
    val defaultPriority: Priority = Priority.NONE,
    val defaultReminderMinutes: Int = 0,
    val swipeRightAction: SwipeAction = SwipeAction.COMPLETE,
    val swipeLeftAction: SwipeAction = SwipeAction.DELETE,
    val completedTaskBehavior: CompletedTaskBehavior = CompletedTaskBehavior.SHOW_AT_BOTTOM,
    val hapticsEnabled: Boolean = true,
    val soundEnabled: Boolean = true,
    val appLockEnabled: Boolean = false,
    val autoLockTimeoutSeconds: Int = 0, // 0 = immediately
    val hideSensitiveNotifications: Boolean = false,
    val hideInRecents: Boolean = false,
    val dailySummaryEnabled: Boolean = true,
    val dailySummaryTimeMillis: Long = 9 * 60 * 60 * 1000L, // 9:00 AM
    val overdueNudgesEnabled: Boolean = true,
    val pomodoroWorkDurationMin: Int = 25,
    val pomodoroBreakDurationMin: Int = 5
)
