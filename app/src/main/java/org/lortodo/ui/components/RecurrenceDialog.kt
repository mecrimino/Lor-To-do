package org.lortodo.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.lortodo.domain.model.RecurrenceFrequency
import org.lortodo.domain.model.RecurrenceRule
import org.lortodo.domain.model.RepeatFrom

@Composable
fun RecurrenceDialog(
    initialRule: RecurrenceRule?,
    onDismiss: () -> Unit,
    onConfirm: (RecurrenceRule?) -> Unit
) {
    var selectedFrequency by remember {
        mutableStateOf(initialRule?.frequency ?: RecurrenceFrequency.DAILY)
    }
    var repeatFrom by remember {
        mutableStateOf(initialRule?.repeatFrom ?: RepeatFrom.DUE_DATE)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Repeat Task") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Frequency",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    RecurrenceFrequency.entries.filter { it != RecurrenceFrequency.CUSTOM }.forEach { freq ->
                        FilterChip(
                            selected = selectedFrequency == freq,
                            onClick = { selectedFrequency = freq },
                            label = { Text(freq.name.lowercase().replaceFirstChar { it.uppercase() }) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Repeat From",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    RadioButton(
                        selected = repeatFrom == RepeatFrom.DUE_DATE,
                        onClick = { repeatFrom = RepeatFrom.DUE_DATE }
                    )
                    Text("Due date", style = MaterialTheme.typography.bodyMedium)
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    RadioButton(
                        selected = repeatFrom == RepeatFrom.COMPLETION_DATE,
                        onClick = { repeatFrom = RepeatFrom.COMPLETION_DATE }
                    )
                    Text("Completion date", style = MaterialTheme.typography.bodyMedium)
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val rule = RecurrenceRule(
                        frequency = selectedFrequency,
                        repeatFrom = repeatFrom
                    )
                    onConfirm(rule)
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            Row {
                if (initialRule != null) {
                    TextButton(onClick = { onConfirm(null) }) {
                        Text("Remove")
                    }
                }
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        }
    )
}
