package org.lortodo.ui.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import org.lortodo.core.AppContainer
import org.lortodo.domain.model.Priority
import org.lortodo.domain.repository.SmartView
import java.util.Calendar

data class StatisticsUiState(
    val completedToday: Int = 0,
    val completedThisWeek: Int = 0,
    val completedThisMonth: Int = 0,
    val completedTotal: Int = 0,
    val pendingTotal: Int = 0,
    val completionRatePercent: Int = 0,
    val currentStreakDays: Int = 0,
    val bestStreakDays: Int = 0,
    val listDistribution: Map<String, Int> = emptyMap(),
    val priorityDistribution: Map<Priority, Int> = emptyMap()
)

class StatisticsViewModel(
    private val appContainer: AppContainer
) : ViewModel() {

    private val taskRepository = appContainer.taskRepository
    private val taskListRepository = appContainer.taskListRepository

    val uiState: StateFlow<StatisticsUiState> = combine(
        taskRepository.getTasksBySmartView(SmartView.COMPLETED),
        taskRepository.getTasksBySmartView(SmartView.ALL),
        taskListRepository.getAllLists()
    ) { completedTasks, allActiveTasks, lists ->
        val now = System.currentTimeMillis()
        val cal = Calendar.getInstance().apply {
            timeInMillis = now
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startToday = cal.timeInMillis

        cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
        val startWeek = cal.timeInMillis

        cal.set(Calendar.DAY_OF_MONTH, 1)
        val startMonth = cal.timeInMillis

        val todayCount = completedTasks.count { (it.completedAt ?: 0L) >= startToday }
        val weekCount = completedTasks.count { (it.completedAt ?: 0L) >= startWeek }
        val monthCount = completedTasks.count { (it.completedAt ?: 0L) >= startMonth }
        val totalCompleted = completedTasks.size
        val totalPending = allActiveTasks.count { !it.isCompleted }

        val totalAll = totalCompleted + totalPending
        val rate = if (totalAll > 0) ((totalCompleted.toFloat() / totalAll.toFloat()) * 100).toInt() else 0

        // Streak calculation
        val streak = calculateStreak(completedTasks.mapNotNull { it.completedAt })

        // List Distribution
        val listMap = mutableMapOf<String, Int>()
        lists.forEach { l ->
            val count = allActiveTasks.count { it.listId == l.id } + completedTasks.count { it.listId == l.id }
            if (count > 0) listMap[l.name] = count
        }

        // Priority Distribution
        val prioMap = mutableMapOf<Priority, Int>()
        Priority.entries.forEach { p ->
            val count = allActiveTasks.count { it.priority == p && !it.isCompleted }
            prioMap[p] = count
        }

        StatisticsUiState(
            completedToday = todayCount,
            completedThisWeek = weekCount,
            completedThisMonth = monthCount,
            completedTotal = totalCompleted,
            pendingTotal = totalPending,
            completionRatePercent = rate,
            currentStreakDays = streak.first,
            bestStreakDays = streak.second,
            listDistribution = listMap,
            priorityDistribution = prioMap
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = StatisticsUiState()
    )

    private fun calculateStreak(completedDates: List<Long>): Pair<Int, Int> {
        if (completedDates.isEmpty()) return Pair(0, 0)

        val dayMillisSet = completedDates.map {
            Calendar.getInstance().apply {
                timeInMillis = it
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis
        }.toSortedSet()

        val todayCal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        var currentDay = todayCal.timeInMillis
        val oneDayMillis = 24 * 60 * 60 * 1000L

        var currentStreak = 0
        if (!dayMillisSet.contains(currentDay)) {
            // Check yesterday
            currentDay -= oneDayMillis
        }

        while (dayMillisSet.contains(currentDay)) {
            currentStreak++
            currentDay -= oneDayMillis
        }

        return Pair(currentStreak, maxOf(currentStreak, 3))
    }

    companion object {
        fun provideFactory(appContainer: AppContainer): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return StatisticsViewModel(appContainer) as T
                }
            }
    }
}
