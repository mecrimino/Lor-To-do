package org.lortodo.ui.search

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
import org.lortodo.domain.model.Priority
import org.lortodo.domain.model.Task

data class SearchUiState(
    val query: String = "",
    val results: List<Task> = emptyList(),
    val recentSearches: List<String> = listOf("Important", "Work", "Bills"),
    val priorityFilter: Priority? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModel(
    private val appContainer: AppContainer
) : ViewModel() {

    private val taskRepository = appContainer.taskRepository
    private val _query = MutableStateFlow("")
    private val _priorityFilter = MutableStateFlow<Priority?>(null)
    private val _recentSearches = MutableStateFlow(listOf("urgent", "bills", "groceries"))

    val uiState: StateFlow<SearchUiState> = combine(
        _query,
        _priorityFilter,
        _recentSearches
    ) { query, priority, recent ->
        Triple(query, priority, recent)
    }.flatMapLatest { (query, priority, recent) ->
        taskRepository.searchTasks(query).combine(_priorityFilter) { tasks, prio ->
            val filtered = if (prio != null) {
                tasks.filter { it.priority == prio }
            } else {
                tasks
            }
            SearchUiState(
                query = query,
                results = filtered,
                recentSearches = recent,
                priorityFilter = priority
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SearchUiState()
    )

    fun onQueryChange(newQuery: String) {
        _query.value = newQuery
    }

    fun onPriorityFilterSelect(priority: Priority?) {
        _priorityFilter.value = priority
    }

    fun addRecentSearch(search: String) {
        if (search.isNotBlank()) {
            val current = _recentSearches.value.toMutableList()
            current.remove(search)
            current.add(0, search)
            _recentSearches.value = current.take(5)
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
                    return SearchViewModel(appContainer) as T
                }
            }
    }
}
