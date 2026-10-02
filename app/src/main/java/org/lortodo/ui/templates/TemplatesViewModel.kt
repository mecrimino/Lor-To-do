package org.lortodo.ui.templates

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.lortodo.core.AppContainer
import org.lortodo.domain.model.Priority
import org.lortodo.domain.model.Subtask
import org.lortodo.domain.model.Task
import org.lortodo.domain.model.TaskTemplate

data class TemplatesUiState(
    val templates: List<TaskTemplate> = emptyList(),
    val message: String? = null
)

class TemplatesViewModel(
    private val appContainer: AppContainer
) : ViewModel() {

    private val templateRepository = appContainer.templateRepository
    private val taskRepository = appContainer.taskRepository
    private val _message = MutableStateFlow<String?>(null)

    val uiState: StateFlow<TemplatesUiState> = combine(
        templateRepository.getAllTemplates(),
        _message
    ) { templates, msg ->
        TemplatesUiState(templates = templates, message = msg)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TemplatesUiState()
    )

    fun deleteTemplate(id: Long) {
        viewModelScope.launch {
            templateRepository.deleteTemplate(id)
            _message.value = "Template deleted"
        }
    }

    fun createTaskFromTemplate(template: TaskTemplate, onCreated: (Long) -> Unit) {
        viewModelScope.launch {
            try {
                val json = Json { ignoreUnknownKeys = true }
                val root = json.parseToJsonElement(template.payloadJson).jsonObject
                val title = root["title"]?.jsonPrimitive?.content ?: template.name
                val notes = root["notes"]?.jsonPrimitive?.content ?: ""
                val priorityStr = root["priority"]?.jsonPrimitive?.content ?: "NONE"
                val priority = try { Priority.valueOf(priorityStr) } catch (_: Exception) { Priority.NONE }
                val estimateMinutes = root["estimateMinutes"]?.jsonPrimitive?.content?.toIntOrNull()

                val subtasks = mutableListOf<Subtask>()
                root["subtasks"]?.jsonArray?.forEach { element ->
                    val sObj = element.jsonObject
                    val sTitle = sObj["title"]?.jsonPrimitive?.content ?: ""
                    val sOrder = sObj["sortOrder"]?.jsonPrimitive?.content?.toIntOrNull() ?: 0
                    if (sTitle.isNotBlank()) {
                        subtasks.add(Subtask(title = sTitle, sortOrder = sOrder))
                    }
                }

                val task = Task(
                    title = title,
                    notes = notes,
                    priority = priority,
                    estimateMinutes = estimateMinutes,
                    subtasks = subtasks
                )

                val newTaskId = taskRepository.insertTask(task)
                _message.value = "Task created from '$title'"
                onCreated(newTaskId)
            } catch (e: Exception) {
                _message.value = "Failed to create task from template: ${e.message}"
            }
        }
    }

    fun clearMessage() {
        _message.value = null
    }

    companion object {
        fun provideFactory(appContainer: AppContainer): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return TemplatesViewModel(appContainer) as T
                }
            }
    }
}
