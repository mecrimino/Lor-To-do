package org.lortodo.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.lortodo.domain.model.Priority
import org.lortodo.ui.theme.PriorityHighColor
import org.lortodo.ui.theme.PriorityLowColor
import org.lortodo.ui.theme.PriorityMediumColor

@Composable
fun PrioritySelector(
    selectedPriority: Priority,
    onPrioritySelected: (Priority) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Priority.entries.forEach { priority ->
            val isSelected = priority == selectedPriority
            val chipColor = when (priority) {
                Priority.HIGH -> PriorityHighColor
                Priority.MEDIUM -> PriorityMediumColor
                Priority.LOW -> PriorityLowColor
                Priority.NONE -> Color.Gray
            }

            FilterChip(
                selected = isSelected,
                onClick = { onPrioritySelected(priority) },
                label = {
                    Text(
                        text = priority.label,
                        color = if (isSelected) chipColor else MaterialTheme.colorScheme.onSurface
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = chipColor.copy(alpha = 0.2f),
                    selectedLabelColor = chipColor
                )
            )
        }
    }
}
