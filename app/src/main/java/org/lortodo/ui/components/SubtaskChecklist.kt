package org.lortodo.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import org.lortodo.domain.model.Subtask

@Composable
fun SubtaskChecklist(
    subtasks: List<Subtask>,
    onToggleSubtask: (Subtask) -> Unit,
    onAddSubtask: (String) -> Unit,
    onDeleteSubtask: (Subtask) -> Unit,
    modifier: Modifier = Modifier
) {
    var newSubtaskText by remember { mutableStateOf("") }

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Subtasks (${subtasks.count { it.isDone }}/${subtasks.size})",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )

        // Existing Subtask Items
        subtasks.forEach { subtask ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = subtask.isDone,
                    onCheckedChange = { onToggleSubtask(subtask) },
                    modifier = Modifier.size(32.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = subtask.title,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        textDecoration = if (subtask.isDone) TextDecoration.LineThrough else TextDecoration.None
                    ),
                    color = if (subtask.isDone) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onToggleSubtask(subtask) }
                )

                IconButton(
                    onClick = { onDeleteSubtask(subtask) },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Remove subtask",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }

        // Add Subtask Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = newSubtaskText,
                onValueChange = { newSubtaskText = it },
                placeholder = { Text("Add subtask...") },
                modifier = Modifier.weight(1f),
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(
                    onDone = {
                        if (newSubtaskText.isNotBlank()) {
                            onAddSubtask(newSubtaskText.trim())
                            newSubtaskText = ""
                        }
                    }
                )
            )

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = {
                    if (newSubtaskText.isNotBlank()) {
                        onAddSubtask(newSubtaskText.trim())
                        newSubtaskText = ""
                    }
                },
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add subtask",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
