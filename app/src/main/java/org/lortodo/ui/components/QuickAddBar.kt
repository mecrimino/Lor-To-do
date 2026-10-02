package org.lortodo.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.lortodo.domain.engine.NaturalLanguageTaskParser
import org.lortodo.domain.engine.ParsedTaskResult
import org.lortodo.domain.model.Priority
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun QuickAddBar(
    onAddTask: (ParsedTaskResult) -> Unit,
    modifier: Modifier = Modifier
) {
    var text by remember { mutableStateOf("") }
    val parsedResult = remember(text) {
        if (text.isNotBlank()) NaturalLanguageTaskParser.parse(text) else null
    }

    val hasParsedTokens = parsedResult != null && (
        parsedResult.dueDate != null ||
        parsedResult.priority != Priority.NONE ||
        parsedResult.tags.isNotEmpty() ||
        parsedResult.recurrenceRule != null
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .imePadding()
            .navigationBarsPadding(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp,
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Live parsed tokens badges preview
            AnimatedVisibility(visible = hasParsedTokens && parsedResult != null) {
                if (parsedResult != null) {
                    FlowRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (parsedResult.dueDate != null) {
                            val df = SimpleDateFormat("EEE, MMM d", Locale.getDefault())
                            val tf = SimpleDateFormat("h:mm a", Locale.getDefault())
                            val dateStr = buildString {
                                append(df.format(Date(parsedResult.dueDate)))
                                if (!parsedResult.isAllDay && parsedResult.dueTime != null) {
                                    append(" ${tf.format(Date(parsedResult.dueDate + parsedResult.dueTime))}")
                                }
                            }
                            AssistChip(
                                onClick = {},
                                label = { Text("📅 $dateStr", fontSize = 11.sp) },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                                    labelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            )
                        }

                        if (parsedResult.priority != Priority.NONE) {
                            AssistChip(
                                onClick = {},
                                label = { Text("⚡ ${parsedResult.priority.name}", fontSize = 11.sp) },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer,
                                    labelColor = MaterialTheme.colorScheme.onErrorContainer
                                )
                            )
                        }

                        parsedResult.tags.forEach { tag ->
                            AssistChip(
                                onClick = {},
                                label = { Text("#$tag", fontSize = 11.sp) },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                    labelColor = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            )
                        }

                        if (parsedResult.recurrenceRule != null) {
                            AssistChip(
                                onClick = {},
                                label = { Text("🔁 ${parsedResult.recurrenceRule.toReadableString()}", fontSize = 11.sp) }
                            )
                        }
                    }
                }
            }

            // Input Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    placeholder = {
                        Text(
                            text = "Add task... e.g. Pay rent tomorrow 6pm !high #bills",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(24.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f),
                        focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            if (text.isNotBlank()) {
                                val result = NaturalLanguageTaskParser.parse(text)
                                onAddTask(result)
                                text = ""
                            }
                        }
                    )
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Submit Button
                IconButton(
                    onClick = {
                        if (text.isNotBlank()) {
                            val result = NaturalLanguageTaskParser.parse(text)
                            onAddTask(result)
                            text = ""
                        }
                    },
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(
                            if (text.isNotBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowUpward,
                        contentDescription = "Add Task",
                        tint = if (text.isNotBlank()) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
