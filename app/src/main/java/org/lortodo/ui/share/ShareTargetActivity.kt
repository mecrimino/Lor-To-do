package org.lortodo.ui.share

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.lortodo.LorTodoApp
import org.lortodo.domain.engine.NaturalLanguageTaskParser
import org.lortodo.domain.model.Task
import org.lortodo.ui.theme.LorTodoTheme

class ShareTargetActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sharedText = intent?.getStringExtra(Intent.EXTRA_TEXT) ?: ""
        if (sharedText.isBlank()) {
            Toast.makeText(this, "No content shared", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val app = application as LorTodoApp
        val container = app.appContainer

        setContent {
            LorTodoTheme {
                var taskText by remember { mutableStateOf(sharedText) }

                AlertDialog(
                    onDismissRequest = { finish() },
                    title = { Text("Add to LOR TO-DO") },
                    text = {
                        Column {
                            Text("Create a task from shared content:")
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = taskText,
                                onValueChange = { taskText = it },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                CoroutineScope(Dispatchers.IO).launch {
                                    val parsed = NaturalLanguageTaskParser.parse(taskText)
                                    val task = Task(
                                        title = parsed.title.ifBlank { taskText },
                                        dueDate = parsed.dueDate,
                                        dueTime = parsed.dueTime,
                                        isAllDay = parsed.isAllDay,
                                        priority = parsed.priority,
                                        recurrenceRule = parsed.recurrenceRule
                                    )
                                    container.taskRepository.insertTask(task)
                                    runOnUiThread {
                                        Toast.makeText(this@ShareTargetActivity, "Task saved!", Toast.LENGTH_SHORT).show()
                                        finish()
                                    }
                                }
                            }
                        ) {
                            Text("Save")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { finish() }) {
                            Text("Cancel")
                        }
                    }
                )
            }
        }
    }
}
