package org.lortodo.ui.navigation

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object TaskDetail : Screen("task_detail/{taskId}") {
        fun createRoute(taskId: Long): String = "task_detail/$taskId"
    }
    data object Calendar : Screen("calendar")
    data object Kanban : Screen("kanban")
    data object Search : Screen("search")
    data object FocusTimer : Screen("focus_timer")
    data object Statistics : Screen("statistics")
    data object ListsAndTags : Screen("lists_and_tags")
    data object Templates : Screen("templates")
    data object TrashArchive : Screen("trash_archive")
    data object Settings : Screen("settings")
    data object About : Screen("about")
}
