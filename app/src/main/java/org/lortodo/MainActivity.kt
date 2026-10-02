package org.lortodo

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import org.lortodo.domain.model.UserPreferences
import org.lortodo.ui.calendar.CalendarScreen
import org.lortodo.ui.calendar.CalendarViewModel
import org.lortodo.ui.detail.TaskDetailScreen
import org.lortodo.ui.detail.TaskDetailViewModel
import org.lortodo.ui.focus.FocusTimerScreen
import org.lortodo.ui.focus.FocusTimerViewModel
import org.lortodo.ui.home.HomeScreen
import org.lortodo.ui.home.HomeViewModel
import org.lortodo.ui.kanban.KanbanScreen
import org.lortodo.ui.kanban.KanbanViewModel
import org.lortodo.ui.lists.ListsAndTagsScreen
import org.lortodo.ui.lists.ListsAndTagsViewModel
import org.lortodo.ui.lock.AppLockScreen
import org.lortodo.ui.navigation.Screen
import org.lortodo.ui.search.SearchScreen
import org.lortodo.ui.search.SearchViewModel
import org.lortodo.ui.settings.AboutScreen
import org.lortodo.ui.settings.SettingsScreen
import org.lortodo.ui.settings.SettingsViewModel
import org.lortodo.ui.statistics.StatisticsScreen
import org.lortodo.ui.statistics.StatisticsViewModel
import org.lortodo.ui.theme.LorTodoTheme
import org.lortodo.ui.trash.TrashArchiveScreen
import org.lortodo.ui.trash.TrashArchiveViewModel

class MainActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as LorTodoApp
        val container = app.appContainer

        // Handle initial intent
        val initialTaskId = extractTaskIdFromIntent(intent)
        val initialAction = intent.getStringExtra("action")

        setContent {
            val userPrefs by container.userPreferencesRepository.userPreferences.collectAsState(initial = UserPreferences())

            // Update FLAG_SECURE dynamically based on hideInRecents preference
            if (userPrefs.hideInRecents) {
                window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)
            } else {
                window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
            }

            LorTodoTheme(
                appTheme = userPrefs.theme,
                accentColor = userPrefs.accentColor
            ) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    var isUnlocked by remember { mutableStateOf(!userPrefs.appLockEnabled) }

                    if (userPrefs.appLockEnabled && !isUnlocked) {
                        AppLockScreen(onUnlockSuccess = { isUnlocked = true })
                    } else {
                        MainAppNavigation(
                            appContainer = container,
                            initialTaskId = initialTaskId,
                            initialAction = initialAction
                        )
                    }
                }
            }
        }
    }

    private fun extractTaskIdFromIntent(intent: Intent?): Long? {
        val data = intent?.data ?: return null
        if (data.scheme == "lortodo" && data.host == "task") {
            val path = data.lastPathSegment
            return path?.toLongOrNull()
        }
        return null
    }
}

@Composable
fun MainAppNavigation(
    appContainer: org.lortodo.core.AppContainer,
    initialTaskId: Long?,
    initialAction: String?
) {
    val navController = rememberNavController()

    val startDestination = when {
        initialTaskId != null -> Screen.TaskDetail.createRoute(initialTaskId)
        initialAction == "search" -> Screen.Search.route
        else -> Screen.Home.route
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Home.route) {
            val homeViewModel: HomeViewModel = viewModel(
                factory = HomeViewModel.provideFactory(appContainer)
            )
            HomeScreen(
                viewModel = homeViewModel,
                onNavigateToTaskDetail = { taskId ->
                    navController.navigate(Screen.TaskDetail.createRoute(taskId))
                },
                onNavigateToCalendar = { navController.navigate(Screen.Calendar.route) },
                onNavigateToKanban = { navController.navigate(Screen.Kanban.route) },
                onNavigateToSearch = { navController.navigate(Screen.Search.route) },
                onNavigateToFocusTimer = { navController.navigate(Screen.FocusTimer.route) },
                onNavigateToStatistics = { navController.navigate(Screen.Statistics.route) },
                onNavigateToListsAndTags = { navController.navigate(Screen.ListsAndTags.route) },
                onNavigateToTemplates = { navController.navigate(Screen.Templates.route) },
                onNavigateToTrashArchive = { navController.navigate(Screen.TrashArchive.route) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                onNavigateToAbout = { navController.navigate(Screen.About.route) }
            )
        }

        composable(
            route = Screen.TaskDetail.route,
            arguments = listOf(navArgument("taskId") { type = NavType.LongType })
        ) { backStackEntry ->
            val taskId = backStackEntry.arguments?.getLong("taskId") ?: 0L
            val detailViewModel: TaskDetailViewModel = viewModel(
                key = "task_$taskId",
                factory = TaskDetailViewModel.provideFactory(appContainer, taskId)
            )
            TaskDetailScreen(
                viewModel = detailViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Calendar.route) {
            val calendarViewModel: CalendarViewModel = viewModel(
                factory = CalendarViewModel.provideFactory(appContainer)
            )
            CalendarScreen(
                viewModel = calendarViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToTaskDetail = { taskId ->
                    navController.navigate(Screen.TaskDetail.createRoute(taskId))
                }
            )
        }

        composable(Screen.Kanban.route) {
            val kanbanViewModel: KanbanViewModel = viewModel(
                factory = KanbanViewModel.provideFactory(appContainer)
            )
            KanbanScreen(
                viewModel = kanbanViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToTaskDetail = { taskId ->
                    navController.navigate(Screen.TaskDetail.createRoute(taskId))
                }
            )
        }

        composable(Screen.Search.route) {
            val searchViewModel: SearchViewModel = viewModel(
                factory = SearchViewModel.provideFactory(appContainer)
            )
            SearchScreen(
                viewModel = searchViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToTaskDetail = { taskId ->
                    navController.navigate(Screen.TaskDetail.createRoute(taskId))
                }
            )
        }

        composable(Screen.FocusTimer.route) {
            val focusViewModel: FocusTimerViewModel = viewModel(
                factory = FocusTimerViewModel.provideFactory(appContainer)
            )
            FocusTimerScreen(
                viewModel = focusViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Statistics.route) {
            val statsViewModel: StatisticsViewModel = viewModel(
                factory = StatisticsViewModel.provideFactory(appContainer)
            )
            StatisticsScreen(
                viewModel = statsViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.ListsAndTags.route) {
            val listsViewModel: ListsAndTagsViewModel = viewModel(
                factory = ListsAndTagsViewModel.provideFactory(appContainer)
            )
            ListsAndTagsScreen(
                viewModel = listsViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.TrashArchive.route) {
            val trashViewModel: TrashArchiveViewModel = viewModel(
                factory = TrashArchiveViewModel.provideFactory(appContainer)
            )
            TrashArchiveScreen(
                viewModel = trashViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Settings.route) {
            val settingsViewModel: SettingsViewModel = viewModel(
                factory = SettingsViewModel.provideFactory(appContainer)
            )
            SettingsScreen(
                viewModel = settingsViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToAbout = { navController.navigate(Screen.About.route) }
            )
        }

        composable(Screen.Templates.route) {
            val templatesViewModel: org.lortodo.ui.templates.TemplatesViewModel = viewModel(
                factory = org.lortodo.ui.templates.TemplatesViewModel.provideFactory(appContainer)
            )
            org.lortodo.ui.templates.TemplatesScreen(
                viewModel = templatesViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToTaskDetail = { taskId ->
                    navController.navigate(Screen.TaskDetail.createRoute(taskId))
                }
            )
        }

        composable(Screen.About.route) {
            AboutScreen(onNavigateBack = { navController.popBackStack() })
        }
    }
}
