package com.example.coba2

import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.coba2.model.Task
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import android.util.Log



class AddTaskActivity : AppCompatActivity() {

    private var taskReminderTime: Calendar? = null
    private lateinit var taskTitle: EditText
    private lateinit var taskDescription: EditText
    private lateinit var taskTimePicker: TextView
    private lateinit var saveTaskButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_task)

        taskTitle = findViewById(R.id.task_title)
        taskDescription = findViewById(R.id.task_description)
        taskTimePicker = findViewById(R.id.task_time_picker)
        saveTaskButton = findViewById(R.id.save_task_button)

        taskTimePicker.setOnClickListener {
            showTimePickerDialog()
        }

        saveTaskButton.setOnClickListener {
            saveTask()
        }
    }

    private fun showTimePickerDialog() {
        val currentTime = Calendar.getInstance()

        TimePickerDialog(
            this,
            { _, hourOfDay, minute ->
                // Create a new Calendar instance for the selected time
                val selectedTime = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, hourOfDay)
                    set(Calendar.MINUTE, minute)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }

                // If selected time is earlier than current time, add one day
                if (selectedTime.before(currentTime)) {
                    selectedTime.add(Calendar.DAY_OF_MONTH, 1)
                }

                taskReminderTime = selectedTime
                taskTimePicker.text = formatTime(selectedTime)
            },
            currentTime.get(Calendar.HOUR_OF_DAY),
            currentTime.get(Calendar.MINUTE),
            true
        ).show()
    }

    private fun saveTask() {
        val title = taskTitle.text.toString().trim()
        val description = taskDescription.text.toString().trim()

        if (title.isEmpty()) {
            taskTitle.error = "Title cannot be empty"
            return
        }

        if (taskReminderTime == null) {
            Toast.makeText(this, "Please select a time", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val task = Task(title = title, description = description, reminderTime = taskReminderTime)
            val intent = Intent().apply {
                putExtra("task", task)
            }
            setResult(RESULT_OK, intent)
            Log.d("AddTaskActivity", "Task created: $task")

            finish()

        } catch (e: Exception) {
            Toast.makeText(this, "Error saving task: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun formatTime(calendar: Calendar): String {
        val format = SimpleDateFormat("HH:mm", Locale.getDefault())
        return format.format(calendar.time)
    }
}