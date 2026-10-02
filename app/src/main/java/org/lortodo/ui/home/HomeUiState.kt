package org.lortodo.ui.home

import org.lortodo.domain.model.Priority
import org.lortodo.domain.model.SavedFilter
import org.lortodo.domain.model.Task
import org.lortodo.domain.model.TaskList
import org.lortodo.domain.model.UserPreferences
import org.lortodo.domain.repository.SmartView
import org.lortodo.domain.repository.SortDirection
import org.lortodo.domain.repository.TaskSortBy

data class HomeUiState(
    val activeView: SmartView = SmartView.TODAY,
    val selectedDateMillis: Long = System.currentTimeMillis(),
    val tasks: List<Task> = emptyList(),
    val lists: List<TaskList> = emptyList(),
    val savedFilters: List<SavedFilter> = emptyList(),
    val preferences: UserPreferences = UserPreferences(),
    val sortBy: TaskSortBy = TaskSortBy.MANUAL,
    val sortDirection: SortDirection = SortDirection.ASCENDING,
    val selectedPriorityFilter: Priority? = null,
    val selectedListId: Long? = null,
    val selectedTaskIds: Set<Long> = emptySet(),
    val isLoading: Boolean = false,
    val userMessage: String? = null,
    val lastDeletedTask: Task? = null
) {
    val isSelectionMode: Boolean get() = selectedTaskIds.isNotEmpty()
}
