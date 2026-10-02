package org.lortodo.ui.focus

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.lortodo.core.AppContainer
import org.lortodo.domain.model.FocusSession
import org.lortodo.domain.model.Task
import org.lortodo.domain.repository.SmartView

enum class FocusState {
    IDLE,
    RUNNING,
    PAUSED
}

enum class SessionType(val label: String, val durationMin: Int) {
    WORK("Focus", 25),
    SHORT_BREAK("Short Break", 5),
    LONG_BREAK("Long Break", 15)
}

data class FocusTimerUiState(
    val focusState: FocusState = FocusState.IDLE,
    val sessionType: SessionType = SessionType.WORK,
    val totalSeconds: Int = 25 * 60,
    val remainingSeconds: Int = 25 * 60,
    val selectedTask: Task? = null,
    val availableTasks: List<Task> = emptyList(),
    val completedSessionsToday: Int = 0,
    val todayFocusMinutes: Int = 0
)

class FocusTimerViewModel(
    private val appContainer: AppContainer
) : ViewModel() {

    private val focusRepository = appContainer.focusRepository
    private val taskRepository = appContainer.taskRepository

    private val _uiState = MutableStateFlow(FocusTimerUiState())
    val uiState: StateFlow<FocusTimerUiState> = _uiState

    private var timerJob: Job? = null

    init {
        loadTasks()
    }

    private fun loadTasks() {
        viewModelScope.launch {
            val tasks = taskRepository.getTasksBySmartView(SmartView.TODAY).first()
            _uiState.value = _uiState.value.copy(
                availableTasks = tasks.filter { !it.isCompleted }
            )
        }
    }

    fun selectSessionType(type: SessionType) {
        timerJob?.cancel()
        val secs = type.durationMin * 60
        _uiState.value = _uiState.value.copy(
            sessionType = type,
            totalSeconds = secs,
            remainingSeconds = secs,
            focusState = FocusState.IDLE
        )
    }

    fun selectTask(task: Task?) {
        _uiState.value = _uiState.value.copy(selectedTask = task)
    }

    fun startTimer() {
        if (_uiState.value.focusState == FocusState.RUNNING) return

        _uiState.value = _uiState.value.copy(focusState = FocusState.RUNNING)
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_uiState.value.remainingSeconds > 0) {
                delay(1000L)
                val newSec = _uiState.value.remainingSeconds - 1
                _uiState.value = _uiState.value.copy(remainingSeconds = newSec)
            }
            onTimerFinished()
        }
    }

    fun pauseTimer() {
        timerJob?.cancel()
        _uiState.value = _uiState.value.copy(focusState = FocusState.PAUSED)
    }

    fun resetTimer() {
        timerJob?.cancel()
        val secs = _uiState.value.sessionType.durationMin * 60
        _uiState.value = _uiState.value.copy(
            remainingSeconds = secs,
            focusState = FocusState.IDLE
        )
    }

    private fun onTimerFinished() {
        _uiState.value = _uiState.value.copy(
            focusState = FocusState.IDLE,
            completedSessionsToday = _uiState.value.completedSessionsToday + 1
        )
        // Log session in repository
        viewModelScope.launch {
            focusRepository.insertSession(
                FocusSession(
                    taskId = _uiState.value.selectedTask?.id,
                    startedAt = System.currentTimeMillis() - (_uiState.value.totalSeconds * 1000L),
                    durationSec = _uiState.value.totalSeconds,
                    type = _uiState.value.sessionType.name.lowercase()
                )
            )
        }
    }

    companion object {
        fun provideFactory(appContainer: AppContainer): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return FocusTimerViewModel(appContainer) as T
                }
            }
    }
}
