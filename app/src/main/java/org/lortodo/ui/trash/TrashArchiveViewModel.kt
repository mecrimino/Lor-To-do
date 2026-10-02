package org.lortodo.ui.trash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.lortodo.core.AppContainer
import org.lortodo.domain.model.Task
import org.lortodo.domain.repository.SmartView

data class TrashArchiveUiState(
    val trashTasks: List<Task> = emptyList(),
    val archivedTasks: List<Task> = emptyList()
)

class TrashArchiveViewModel(
    private val appContainer: AppContainer
) : ViewModel() {

    private val taskRepository = appContainer.taskRepository

    val uiState: StateFlow<TrashArchiveUiState> = combine(
        taskRepository.getTasksBySmartView(SmartView.TRASH),
        taskRepository.getTasksBySmartView(SmartView.ARCHIVE)
    ) { trash, archive ->
        TrashArchiveUiState(trashTasks = trash, archivedTasks = archive)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TrashArchiveUiState()
    )

    fun restoreTask(taskId: Long) {
        viewModelScope.launch {
            taskRepository.restoreTask(taskId)
        }
    }

    fun permanentlyDeleteTask(taskId: Long) {
        viewModelScope.launch {
            taskRepository.permanentlyDeleteTask(taskId)
        }
    }

    fun emptyTrash() {
        viewModelScope.launch {
            taskRepository.emptyTrash()
        }
    }

    fun unarchiveTask(taskId: Long) {
        viewModelScope.launch {
            taskRepository.unarchiveTask(taskId)
        }
    }

    companion object {
        fun provideFactory(appContainer: AppContainer): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return TrashArchiveViewModel(appContainer) as T
                }
            }
    }
}
