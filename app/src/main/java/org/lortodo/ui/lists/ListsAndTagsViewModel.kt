package org.lortodo.ui.lists

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
import org.lortodo.domain.model.Tag
import org.lortodo.domain.model.TaskList

data class ListsAndTagsUiState(
    val lists: List<TaskList> = emptyList(),
    val tags: List<Tag> = emptyList()
)

class ListsAndTagsViewModel(
    private val appContainer: AppContainer
) : ViewModel() {

    private val taskListRepository = appContainer.taskListRepository
    private val tagRepository = appContainer.tagRepository

    val uiState: StateFlow<ListsAndTagsUiState> = combine(
        taskListRepository.getAllLists(),
        tagRepository.getAllTags()
    ) { lists, tags ->
        ListsAndTagsUiState(lists = lists, tags = tags)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ListsAndTagsUiState()
    )

    fun createList(name: String, colorHex: String = "#E5A800") {
        if (name.isBlank()) return
        viewModelScope.launch {
            taskListRepository.insertList(
                TaskList(name = name.trim(), colorHex = colorHex)
            )
        }
    }

    fun deleteList(id: Long) {
        viewModelScope.launch {
            taskListRepository.deleteList(id)
        }
    }

    fun createTag(name: String, colorHex: String = "#E5A800") {
        if (name.isBlank()) return
        viewModelScope.launch {
            tagRepository.getOrCreateTag(name.trim().removePrefix("#"), colorHex)
        }
    }

    fun deleteTag(id: Long) {
        viewModelScope.launch {
            tagRepository.deleteTag(id)
        }
    }

    companion object {
        fun provideFactory(appContainer: AppContainer): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return ListsAndTagsViewModel(appContainer) as T
                }
            }
    }
}
