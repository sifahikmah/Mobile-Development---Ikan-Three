package com.example.coba2

import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.coba2.model.Task
import android.content.Intent
import android.widget.TextView
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale



class EditTaskActivity : AppCompatActivity() {
    private lateinit var editTitle: EditText
    private lateinit var editDescription: EditText
    private lateinit var btnSave: Button
    private lateinit var editTime: TextView
    private var taskReminderTime: Calendar? = null
    private var originalTask: Task? = null



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_task)

        editTitle = findViewById(R.id.edit_task_title)
        editDescription = findViewById(R.id.edit_task_description)
        editTime = findViewById(R.id.task_time_picker)
        btnSave = findViewById(R.id.btn_save_task)

        // Mengambil data task dari intent
        originalTask = intent.getParcelableExtra<Task>("task")

        // Jika task ada, isi EditText dengan data task
        originalTask?.let {
            editTitle.setText(it.title)
            editDescription.setText(it.description)
            taskReminderTime = it.reminderTime
            editTime.text = formatTime(it.reminderTime)
        }

        editTime.setOnClickListener {
            showTimePickerDialog()
        }

        // Set aksi untuk tombol simpan
        btnSave.setOnClickListener {
            saveUpdatedTask()
        }
    }

    private fun showTimePickerDialog() {
        val currentTime = taskReminderTime ?: Calendar.getInstance()

        TimePickerDialog(
            this,
            { _, hourOfDay, minute ->
                val selectedTime = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, hourOfDay)
                    set(Calendar.MINUTE, minute)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }

                // If selected time is earlier than current time, add one day
                if (selectedTime.before(Calendar.getInstance())) {
                    selectedTime.add(Calendar.DAY_OF_MONTH, 1)
                }

                taskReminderTime = selectedTime
                editTime.text = formatTime(selectedTime)
            },
            currentTime.get(Calendar.HOUR_OF_DAY),
            currentTime.get(Calendar.MINUTE),
            true
        ).show()
    }

    private fun saveUpdatedTask() {
        val title = editTitle.text.toString().trim()
        val description = editDescription.text.toString().trim()

        if (title.isEmpty()) {
            editTitle.error = "Title cannot be empty"
            return
        }

        if (taskReminderTime == null) {
            Toast.makeText(this, "Please select a time", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            originalTask?.let { original ->
                val updatedTask = Task(
                    id = original.id,
                    title = title,
                    description = description,
                    reminderTime = taskReminderTime,
                    isChecked = original.isChecked
                )

                val intent = Intent().apply {
                    putExtra("updated_task", updatedTask)
                }
                setResult(RESULT_OK, intent)
                finish()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error updating task: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun formatTime(calendar: Calendar?): String {
        if (calendar == null) return "Select Time"
        val format = SimpleDateFormat("HH:mm", Locale.getDefault())
        return format.format(calendar.time)
    }
}
