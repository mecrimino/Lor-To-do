package org.lortodo.ui.kanban

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.lortodo.core.AppContainer
import org.lortodo.domain.model.Task
import org.lortodo.domain.repository.SmartView

data class KanbanUiState(
    val todoTasks: List<Task> = emptyList(),
    val inProgressTasks: List<Task> = emptyList(),
    val doneTasks: List<Task> = emptyList()
)

class KanbanViewModel(
    private val appContainer: AppContainer
) : ViewModel() {

    private val taskRepository = appContainer.taskRepository

    val uiState: StateFlow<KanbanUiState> = combine(
        taskRepository.getTasksBySmartView(SmartView.ALL),
        taskRepository.getTasksBySmartView(SmartView.COMPLETED)
    ) { allTasks, completedTasks ->
        val todo = mutableListOf<Task>()
        val inProgress = mutableListOf<Task>()

        allTasks.forEach { task ->
            if (task.subtasks.isNotEmpty() && task.subtasks.any { it.isDone } && !task.isCompleted) {
                inProgress.add(task)
            } else if (!task.isCompleted) {
                todo.add(task)
            }
        }

        KanbanUiState(
            todoTasks = todo,
            inProgressTasks = inProgress,
            doneTasks = completedTasks
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = KanbanUiState()
    )

    fun moveToDone(task: Task) {
        viewModelScope.launch {
            taskRepository.setTaskCompleted(task.id, true)
        }
    }

    fun moveToTodo(task: Task) {
        viewModelScope.launch {
            taskRepository.setTaskCompleted(task.id, false)
        }
    }

    companion object {
        fun provideFactory(appContainer: AppContainer): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return KanbanViewModel(appContainer) as T
                }
            }
    }
}
