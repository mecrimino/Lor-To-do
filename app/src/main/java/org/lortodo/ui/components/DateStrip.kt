package org.lortodo.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class DateItem(
    val dayOfWeek: String, // "Mo", "Tu", etc.
    val dayOfMonth: Int,   // 8
    val dateMillis: Long,  // Start of day millis
    val isToday: Boolean
)

@Composable
fun DateStrip(
    selectedDateMillis: Long,
    onDateSelected: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val calendar = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    val todayMillis = calendar.timeInMillis

    // Format current month (e.g. "May")
    val monthFormat = SimpleDateFormat("MMMM", Locale.getDefault())
    val currentMonthName = monthFormat.format(Date(selectedDateMillis))

    // Align to Monday of the week containing selectedDateMillis
    val weekCalendar = Calendar.getInstance().apply {
        timeInMillis = selectedDateMillis
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
        val dow = get(Calendar.DAY_OF_WEEK)
        // ISO Monday alignment
        val diff = if (dow == Calendar.SUNDAY) -6 else Calendar.MONDAY - dow
        add(Calendar.DAY_OF_MONTH, diff)
    }

    val dayFormat = SimpleDateFormat("EE", Locale.getDefault())
    val days = (0..6).map {
        val itemCal = (weekCalendar.clone() as Calendar).apply {
            add(Calendar.DAY_OF_MONTH, it)
        }
        val millis = itemCal.timeInMillis
        val rawDow = dayFormat.format(Date(millis))
        val shortDow = rawDow.take(2) // "Mo", "Tu", etc.
        val dayNum = itemCal.get(Calendar.DAY_OF_MONTH)
        DateItem(
            dayOfWeek = shortDow,
            dayOfMonth = dayNum,
            dateMillis = millis,
            isToday = millis == todayMillis
        )
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Month Title centered as in mockup
        Text(
            text = currentMonthName,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            ),
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Week row: Mo Tu We Th Fr Sa Su
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            days.forEach { item ->
                val isSelected = item.dateMillis == selectedDateMillis

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clickable { onDateSelected(item.dateMillis) }
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = item.dayOfWeek,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Medium,
                            fontSize = 13.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = item.dayOfMonth.toString(),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = if (isSelected || item.isToday) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 15.sp
                            ),
                            color = if (isSelected) {
                                MaterialTheme.colorScheme.onPrimary
                            } else if (item.isToday) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            }
                        )
                    }
                }
            }
        }
    }
}
