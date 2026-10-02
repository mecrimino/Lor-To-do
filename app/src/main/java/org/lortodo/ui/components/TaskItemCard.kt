package org.lortodo.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.lortodo.domain.model.Priority
import org.lortodo.domain.model.Task
import org.lortodo.ui.theme.PriorityHighColor
import org.lortodo.ui.theme.PriorityLowColor
import org.lortodo.ui.theme.PriorityMediumColor
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class, ExperimentalFoundationApi::class)
@Composable
fun TaskItemCard(
    task: Task,
    onToggleComplete: () -> Unit,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onToggleStar: () -> Unit = {},
    isSelected: Boolean = false,
    isSelectionMode: Boolean = false,
    onLongClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (isSelectionMode) {
                false
            } else if (value == SwipeToDismissBoxValue.StartToEnd) {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onToggleComplete()
                false
            } else if (value == SwipeToDismissBoxValue.EndToStart) {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onDelete()
                true
            } else {
                false
            }
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        modifier = modifier.fillMaxWidth(),
        enableDismissFromStartToEnd = !isSelectionMode,
        enableDismissFromEndToStart = !isSelectionMode,
        backgroundContent = {
            val direction = dismissState.dismissDirection
            val color by animateColorAsState(
                when (dismissState.targetValue) {
                    SwipeToDismissBoxValue.StartToEnd -> Color(0xFF43A047)
                    SwipeToDismissBoxValue.EndToStart -> Color(0xFFE53935)
                    else -> Color.Transparent
                },
                label = "swipe_bg"
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(12.dp))
                    .background(color)
                    .padding(horizontal = 20.dp),
                contentAlignment = if (direction == SwipeToDismissBoxValue.StartToEnd) Alignment.CenterStart else Alignment.CenterEnd
            ) {
                if (direction == SwipeToDismissBoxValue.StartToEnd) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Complete",
                        tint = Color.White
                    )
                } else if (direction == SwipeToDismissBoxValue.EndToStart) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = Color.White
                    )
                }
            }
        }
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .combinedClickable(
                    onClick = {
                        if (isSelectionMode) {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onLongClick?.invoke()
                        } else {
                            onClick()
                        }
                    },
                    onLongClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onLongClick?.invoke()
                    }
                ),
            color = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
            } else {
                MaterialTheme.colorScheme.surface
            },
            shape = RoundedCornerShape(12.dp),
            border = if (isSelected) {
                BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
            } else null,
            tonalElevation = 1.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // In selection mode: show selection checkbox indicator
                if (isSelectionMode) {
                    Icon(
                        imageVector = if (isSelected) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                        contentDescription = if (isSelected) "Selected" else "Not selected",
                        tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                }

                // Priority Marker Bar (if priority set)
                if (task.priority != Priority.NONE) {
                    val priorityColor = when (task.priority) {
                        Priority.HIGH -> PriorityHighColor
                        Priority.MEDIUM -> PriorityMediumColor
                        Priority.LOW -> PriorityLowColor
                        Priority.NONE -> Color.Transparent
                    }
                    Box(
                        modifier = Modifier
                            .size(width = 4.dp, height = 24.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(priorityColor)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                }

                // Square rounded Checkbox matching mockup (when not in multi-selection mode)
                if (!isSelectionMode) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .border(
                                BorderStroke(
                                    width = 1.8.dp,
                                    color = if (task.isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                                ),
                                shape = RoundedCornerShape(6.dp)
                            )
                            .background(
                                if (task.isCompleted) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else Color.Transparent
                            )
                            .combinedClickable(
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onToggleComplete()
                                },
                                onLongClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onLongClick?.invoke()
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (task.isCompleted) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Completed",
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                }

                // Task Details Column
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Medium,
                            textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                        ),
                        color = if (task.isCompleted) {
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        }
                    )

                    // Secondary Info Row (due time, subtask count, tags)
                    if (task.notes.isNotBlank() || task.dueDate != null || task.subtasks.isNotEmpty() || task.tags.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            // Due Date & Time
                            if (task.dueDate != null) {
                                val dateFmt = SimpleDateFormat("MMM d", Locale.getDefault())
                                val timeFmt = SimpleDateFormat("h:mm a", Locale.getDefault())
                                val dateText = buildString {
                                    append(dateFmt.format(Date(task.dueDate)))
                                    if (!task.isAllDay && task.dueTime != null) {
                                        append(" ${timeFmt.format(Date(task.dueDate + task.dueTime))}")
                                    }
                                }

                                Text(
                                    text = dateText,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (task.isOverdue) PriorityHighColor else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            // Subtasks indicator
                            if (task.subtasks.isNotEmpty()) {
                                Text(
                                    text = "☑ ${task.subtasksCompletedCount}/${task.subtasksTotalCount}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            // Tags chips
                            task.tags.forEach { tag ->
                                Text(
                                    text = "#${tag.name}",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 11.sp
                                    ),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }

                // Bell reminder icon if set
                if (task.reminders.isNotEmpty()) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Reminder",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Repeat icon if recurring
                if (task.recurrenceRule != null) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Default.Repeat,
                        contentDescription = "Recurring",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Star Button (hidden during multi-select to avoid accidental clicks)
                if (!isSelectionMode) {
                    IconButton(
                        onClick = onToggleStar,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = if (task.isStarred) "Starred" else "Not Starred",
                            modifier = Modifier.size(18.dp),
                            tint = if (task.isStarred) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                        )
                    }
                }
            }
        }
    }
}
