package org.lortodo.ui.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.lortodo.core.AppContainer
import org.lortodo.domain.model.Task
import java.util.Calendar

data class CalendarUiState(
    val selectedYear: Int,
    val selectedMonth: Int, // 0-based
    val selectedDateMillis: Long,
    val tasksForSelectedDate: List<Task> = emptyList(),
    val monthTaskDates: Set<Long> = emptySet() // Dates in month that have tasks
)

@OptIn(ExperimentalCoroutinesApi::class)
class CalendarViewModel(
    private val appContainer: AppContainer
) : ViewModel() {

    private val taskRepository = appContainer.taskRepository

    private val nowCalendar = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }

    private val _selectedYear = MutableStateFlow(nowCalendar.get(Calendar.YEAR))
    private val _selectedMonth = MutableStateFlow(nowCalendar.get(Calendar.MONTH))
    private val _selectedDateMillis = MutableStateFlow(nowCalendar.timeInMillis)

    val uiState: StateFlow<CalendarUiState> = combine(
        _selectedYear,
        _selectedMonth,
        _selectedDateMillis
    ) { year, month, selectedDate ->
        Triple(year, month, selectedDate)
    }.flatMapLatest { (year, month, selectedDate) ->
        val monthStart = Calendar.getInstance().apply {
            set(year, month, 1, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val monthEnd = Calendar.getInstance().apply {
            set(year, month, 1, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
            add(Calendar.MONTH, 1)
            add(Calendar.MILLISECOND, -1)
        }.timeInMillis

        taskRepository.getTasksForDateRange(monthStart, monthEnd)
            .combine(taskRepository.getTasksForDate(selectedDate)) { monthTasks, dayTasks ->
                val datesWithTasks = monthTasks.mapNotNull { it.dueDate }.map {
                    Calendar.getInstance().apply {
                        timeInMillis = it
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }.timeInMillis
                }.toSet()

                CalendarUiState(
                    selectedYear = year,
                    selectedMonth = month,
                    selectedDateMillis = selectedDate,
                    tasksForSelectedDate = dayTasks,
                    monthTaskDates = datesWithTasks
                )
            }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CalendarUiState(
            selectedYear = nowCalendar.get(Calendar.YEAR),
            selectedMonth = nowCalendar.get(Calendar.MONTH),
            selectedDateMillis = nowCalendar.timeInMillis
        )
    )

    fun selectDate(dateMillis: Long) {
        _selectedDateMillis.value = dateMillis
    }

    fun nextMonth() {
        if (_selectedMonth.value == 11) {
            _selectedMonth.value = 0
            _selectedYear.value += 1
        } else {
            _selectedMonth.value += 1
        }
    }

    fun previousMonth() {
        if (_selectedMonth.value == 0) {
            _selectedMonth.value = 11
            _selectedYear.value -= 1
        } else {
            _selectedMonth.value -= 1
        }
    }

    fun toggleComplete(task: Task) {
        viewModelScope.launch {
            taskRepository.setTaskCompleted(task.id, !task.isCompleted)
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            taskRepository.softDeleteTask(task.id)
        }
    }

    companion object {
        fun provideFactory(appContainer: AppContainer): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return CalendarViewModel(appContainer) as T
                }
            }
    }
}
