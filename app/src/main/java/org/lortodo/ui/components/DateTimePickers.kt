package org.lortodo.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun DueDatePickerSection(
    dueDateMillis: Long?,
    dueTimeMillis: Long?,
    isAllDay: Boolean,
    onDateChanged: (Long?) -> Unit,
    onTimeChanged: (Long?, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    val calendar = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    val todayMillis = calendar.timeInMillis
    calendar.add(Calendar.DAY_OF_YEAR, 1)
    val tomorrowMillis = calendar.timeInMillis
    calendar.add(Calendar.DAY_OF_YEAR, 6)
    val nextWeekMillis = calendar.timeInMillis

    val dateFormat = SimpleDateFormat("EEE, MMM d, yyyy", Locale.getDefault())
    val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())

    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Quick chip: Today
        FilterChip(
            selected = dueDateMillis == todayMillis,
            onClick = { onDateChanged(todayMillis) },
            label = { Text("Today") }
        )

        // Quick chip: Tomorrow
        FilterChip(
            selected = dueDateMillis == tomorrowMillis,
            onClick = { onDateChanged(tomorrowMillis) },
            label = { Text("Tomorrow") }
        )

        // Quick chip: Next Week
        FilterChip(
            selected = dueDateMillis == nextWeekMillis,
            onClick = { onDateChanged(nextWeekMillis) },
            label = { Text("Next Week") }
        )

        // Custom Pick Date Chip
        FilterChip(
            selected = dueDateMillis != null && dueDateMillis != todayMillis && dueDateMillis != tomorrowMillis && dueDateMillis != nextWeekMillis,
            onClick = { showDatePicker = true },
            leadingIcon = {
                Icon(imageVector = Icons.Default.CalendarMonth, contentDescription = null)
            },
            label = {
                Text(
                    text = if (dueDateMillis != null) dateFormat.format(Date(dueDateMillis)) else "Pick Date"
                )
            }
        )

        if (dueDateMillis != null) {
            IconButton(onClick = { onDateChanged(null) }) {
                Icon(imageVector = Icons.Default.Close, contentDescription = "Clear Date")
            }
        }
    }

    // Time & All-Day Toggle Row (visible only when date is set)
    if (dueDateMillis != null) {
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "All Day",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.width(8.dp))
            Switch(
                checked = isAllDay,
                onCheckedChange = { checked ->
                    onTimeChanged(if (checked) null else dueTimeMillis ?: (12 * 3600 * 1000L), checked)
                }
            )

            Spacer(modifier = Modifier.weight(1f))

            if (!isAllDay) {
                TextButton(onClick = { showTimePicker = true }) {
                    Icon(imageVector = Icons.Default.AccessTime, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    val timeDate = Date(dueDateMillis + (dueTimeMillis ?: 0L))
                    Text(timeFormat.format(timeDate))
                }
            }
        }
    }

    // DatePicker Dialog
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = dueDateMillis ?: System.currentTimeMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { onDateChanged(it) }
                    showDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // TimePicker Dialog
    if (showTimePicker) {
        val initialHours = ((dueTimeMillis ?: (12 * 3600 * 1000L)) / (3600 * 1000L)).toInt()
        val initialMinutes = (((dueTimeMillis ?: 0L) % (3600 * 1000L)) / (60 * 1000L)).toInt()
        val timePickerState = rememberTimePickerState(
            initialHour = initialHours,
            initialMinute = initialMinutes,
            is24Hour = false
        )

        Dialog(onDismissRequest = { showTimePicker = false }) {
            androidx.compose.material3.Surface(
                shape = MaterialTheme.shapes.extraLarge,
                tonalElevation = 6.dp
            ) {
                Column(modifier = Modifier.fillMaxWidth().height(480.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    TimePicker(state = timePickerState)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showTimePicker = false }) {
                            Text("Cancel")
                        }
                        TextButton(onClick = {
                            val computedMillis = (timePickerState.hour * 3600L + timePickerState.minute * 60L) * 1000L
                            onTimeChanged(computedMillis, false)
                            showTimePicker = false
                        }) {
                            Text("OK")
                        }
                    }
                }
            }
        }
    }
}
