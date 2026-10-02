package org.lortodo.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import kotlinx.coroutines.flow.first
import org.lortodo.MainActivity
import org.lortodo.data.local.AppDatabase
import org.lortodo.data.local.entity.TaskEntity
import java.util.Calendar

class TodayWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val db = AppDatabase.getInstance(context)
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
        }
        val tasks = db.taskDao().getTodayTasks(cal.timeInMillis).first()

        provideContent {
            GlanceTheme {
                WidgetContent(context = context, tasks = tasks)
            }
        }
    }

    @Composable
    private fun WidgetContent(context: Context, tasks: List<TaskEntity>) {
        val quickAddIntent = Intent(context, MainActivity::class.java).apply {
            action = Intent.ACTION_VIEW
            putExtra("action", "quick_add")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }

        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(Color(0xFFFFFDF6))
                .padding(12.dp)
        ) {
            // Header
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = GlanceModifier.defaultWeight()) {
                    Text(
                        text = "Today",
                        style = TextStyle(
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = androidx.glance.unit.ColorProvider(Color(0xFF1D1B16))
                        )
                    )
                    Text(
                        text = "${tasks.count { !it.isCompleted }} pending",
                        style = TextStyle(
                            fontSize = 12.sp,
                            color = androidx.glance.unit.ColorProvider(Color(0xFF7A7570))
                        )
                    )
                }

                // Plus Add Button
                Box(
                    modifier = GlanceModifier
                        .size(36.dp)
                        .background(Color(0xFFE5A800))
                        .clickable(actionStartActivity(quickAddIntent)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "+",
                        style = TextStyle(
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = androidx.glance.unit.ColorProvider(Color(0xFF1D1B16))
                        )
                    )
                }
            }

            Spacer(modifier = GlanceModifier.height(8.dp))

            // Task List
            if (tasks.isEmpty()) {
                Box(
                    modifier = GlanceModifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "All tasks completed! 🎉",
                        style = TextStyle(
                            fontSize = 14.sp,
                            color = androidx.glance.unit.ColorProvider(Color(0xFF7A7570))
                        )
                    )
                }
            } else {
                LazyColumn(modifier = GlanceModifier.fillMaxSize()) {
                    items(tasks) { task ->
                        Row(
                            modifier = GlanceModifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (task.isCompleted) "✓ " else "○ ",
                                style = TextStyle(
                                    fontSize = 14.sp,
                                    color = androidx.glance.unit.ColorProvider(
                                        if (task.isCompleted) Color(0xFF43A047) else Color(0xFFE5A800)
                                    )
                                )
                            )
                            Spacer(modifier = GlanceModifier.width(4.dp))
                            Text(
                                text = task.title,
                                style = TextStyle(
                                    fontSize = 14.sp,
                                    color = androidx.glance.unit.ColorProvider(
                                        if (task.isCompleted) Color(0xFF9E9E9E) else Color(0xFF1D1B16)
                                    )
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

class TodayWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = TodayWidget()
}
