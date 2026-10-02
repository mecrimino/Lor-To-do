package org.lortodo.ui.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.lortodo.ui.components.TaskItemCard
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    viewModel: CalendarViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToTaskDetail: (Long) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    val monthCalendar = Calendar.getInstance().apply {
        set(uiState.selectedYear, uiState.selectedMonth, 1, 0, 0, 0)
        set(Calendar.MILLISECOND, 0)
    }
    val monthName = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(monthCalendar.time)

    val daysInMonth = monthCalendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    val firstDayOfWeek = monthCalendar.get(Calendar.DAY_OF_WEEK) // 1=Sun, 2=Mon...
    val offsetDays = (firstDayOfWeek - Calendar.MONDAY + 7) % 7

    val dayHeaders = listOf("M", "T", "W", "T", "F", "S", "S")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Calendar", fontWeight = FontWeight.Bold) },
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
        ) {
            // Month Switcher Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { viewModel.previousMonth() }) {
                    Icon(imageVector = Icons.Default.ChevronLeft, contentDescription = "Previous Month")
                }

                Text(
                    text = monthName,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 18.sp),
                    color = MaterialTheme.colorScheme.onSurface
                )

                IconButton(onClick = { viewModel.nextMonth() }) {
                    Icon(imageVector = Icons.Default.ChevronRight, contentDescription = "Next Month")
                }
            }

            // Days of week header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                dayHeaders.forEach { h ->
                    Text(
                        text = h,
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.width(36.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Month Grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(7),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                userScrollEnabled = false
            ) {
                // Empty offset cells before 1st of month
                items(offsetDays) {
                    Spacer(modifier = Modifier.aspectRatio(1f))
                }

                // Days of month
                items(daysInMonth) { index ->
                    val dayNum = index + 1
                    val dayCal = (monthCalendar.clone() as Calendar).apply {
                        set(Calendar.DAY_OF_MONTH, dayNum)
                    }
                    val dayMillis = dayCal.timeInMillis
                    val isSelected = dayMillis == uiState.selectedDateMillis
                    val hasTasks = uiState.monthTaskDates.contains(dayMillis)

                    Box(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .padding(2.dp)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
                            )
                            .clickable { viewModel.selectDate(dayMillis) },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = dayNum.toString(),
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                ),
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                            )
                            if (hasTasks) {
                                Box(
                                    modifier = Modifier
                                        .size(4.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary
                                        )
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Tasks for selected date section
            val selectedDateStr = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault()).format(Date(uiState.selectedDateMillis))
            Text(
                text = selectedDateStr,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 20.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (uiState.tasksForSelectedDate.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No tasks for this date",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                } else {
                    items(uiState.tasksForSelectedDate, key = { it.id }) { task ->
                        TaskItemCard(
                            task = task,
                            onToggleComplete = { viewModel.toggleComplete(task) },
                            onClick = { onNavigateToTaskDetail(task.id) },
                            onDelete = { viewModel.deleteTask(task) }
                        )
                    }
                }
            }
        }
    }
}
