package org.lortodo.ui.search

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.lortodo.domain.model.Priority
import org.lortodo.ui.components.TaskItemCard

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SearchScreen(
    viewModel: SearchViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToTaskDetail: (Long) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    OutlinedTextField(
                        value = uiState.query,
                        onValueChange = { viewModel.onQueryChange(it) },
                        placeholder = { Text("Search tasks...") },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Search, contentDescription = null)
                        },
                        trailingIcon = {
                            if (uiState.query.isNotBlank()) {
                                IconButton(onClick = { viewModel.onQueryChange("") }) {
                                    Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear")
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = 8.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 16.dp)
        ) {
            // Priority Filter Chips Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = uiState.priorityFilter == null,
                    onClick = { viewModel.onPriorityFilterSelect(null) },
                    label = { Text("All") }
                )
                FilterChip(
                    selected = uiState.priorityFilter == Priority.HIGH,
                    onClick = {
                        viewModel.onPriorityFilterSelect(
                            if (uiState.priorityFilter == Priority.HIGH) null else Priority.HIGH
                        )
                    },
                    label = { Text("High") }
                )
                FilterChip(
                    selected = uiState.priorityFilter == Priority.MEDIUM,
                    onClick = {
                        viewModel.onPriorityFilterSelect(
                            if (uiState.priorityFilter == Priority.MEDIUM) null else Priority.MEDIUM
                        )
                    },
                    label = { Text("Medium") }
                )
                FilterChip(
                    selected = uiState.priorityFilter == Priority.LOW,
                    onClick = {
                        viewModel.onPriorityFilterSelect(
                            if (uiState.priorityFilter == Priority.LOW) null else Priority.LOW
                        )
                    },
                    label = { Text("Low") }
                )
            }

            // Recent Searches Chips
            if (uiState.query.isBlank()) {
                Text(
                    text = "Recent Searches",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    uiState.recentSearches.forEach { search ->
                        AssistChip(
                            onClick = {
                                viewModel.onQueryChange(search)
                                viewModel.addRecentSearch(search)
                            },
                            label = { Text(search) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Search Results List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (uiState.results.isEmpty() && uiState.query.isNotBlank()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No tasks found matching \"${uiState.query}\"",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                } else {
                    items(uiState.results, key = { it.id }) { task ->
                        TaskItemCard(
                            task = task,
                            onToggleComplete = { viewModel.toggleComplete(task) },
                            onClick = {
                                viewModel.addRecentSearch(task.title)
                                onNavigateToTaskDetail(task.id)
                            },
                            onDelete = { viewModel.deleteTask(task) }
                        )
                    }
                }
            }
        }
    }
}
