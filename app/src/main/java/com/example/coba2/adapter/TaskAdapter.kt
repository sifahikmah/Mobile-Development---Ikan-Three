package com.example.coba2.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.CompoundButton.OnCheckedChangeListener
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.coba2.R
import com.example.coba2.model.Task
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import android.graphics.Paint

//file ini utk mengatur cara data TodoList ditampilkan dlm RecycleView
//class declaration
class TaskAdapter(
    private val tasks: List<Task>,
    private val onEditClickListener: (Task) -> Unit,
    private val onDeleteClickListener: (Task) -> Unit,
    private val onTaskCheckedChangeListener: (Task, Boolean) -> Unit // Listener untuk perubahan status CheckBox
):
    RecyclerView.Adapter<TaskAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_task, parent, false)
        return ViewHolder(view, onEditClickListener, onDeleteClickListener, onTaskCheckedChangeListener)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val task = tasks[position]
        holder.bind(task)
    }

    override fun getItemCount(): Int {
        return tasks.size
    }

    class ViewHolder(
        itemView: View,
        private val onEditClickListener: (Task) -> Unit,
        private val onDeleteClickListener: (Task) -> Unit,
        private val onTaskCheckedChangeListener: (Task, Boolean) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        private val taskTitle: TextView = itemView.findViewById(R.id.task_title)
        private val taskDescription: TextView = itemView.findViewById(R.id.task_description)
        private val taskReminderTime: TextView = itemView.findViewById(R.id.task_reminder_time)
        private val editButton: ImageButton = itemView.findViewById(R.id.edit_task_button)
        private val deleteButton: ImageButton = itemView.findViewById(R.id.delete_task_button)
        private val taskCheckBox: CheckBox = itemView.findViewById(R.id.task_checkbox)

        //method
        fun bind(task: Task) {
        //mengatur data ke elemen UI
            taskTitle.text = task.title
            taskDescription.text = task.description
            taskReminderTime.text = formatTime(task.reminderTime)

            // Set CheckBox status
            taskCheckBox.isChecked = task.isChecked

            // Apply strike-through and transparency if task is checked
            if (task.isChecked) {
                // Mencoret teks dan mengatur opasitas untuk taskTitle
                taskTitle.paintFlags = taskTitle.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                taskTitle.alpha = 0.5f  // Opasitas 50%

                // Mencoret teks dan mengatur opasitas untuk taskDescription
                taskDescription.paintFlags = taskDescription.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                taskDescription.alpha = 0.5f  // Opasitas 50%

                // Mencoret teks dan mengatur opasitas untuk taskReminderTime
                taskReminderTime.paintFlags = taskReminderTime.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                taskReminderTime.alpha = 0.5f  // Opasitas 50%
            } else {
                // Hapus coretan dan kembalikan opasitas ke normal untuk taskTitle
                taskTitle.paintFlags = taskTitle.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                taskTitle.alpha = 1f  // Opasitas normal

                // Hapus coretan dan kembalikan opasitas ke normal untuk taskDescription
                taskDescription.paintFlags = taskDescription.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                taskDescription.alpha = 1f  // Opasitas normal

                // Hapus coretan dan kembalikan opasitas ke normal untuk taskReminderTime
                taskReminderTime.paintFlags = taskReminderTime.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                taskReminderTime.alpha = 1f  // Opasitas normal
            }


            // Set click listener (utk interaksi pengguna) untuk tombol edit
            editButton.setOnClickListener {
                onEditClickListener(task)
            }


            // Set click listener untuk tombol delete
            deleteButton.setOnClickListener {
                onDeleteClickListener(task)
            }

            // Set listener untuk perubahan status CheckBox
            taskCheckBox.setOnCheckedChangeListener { _, isChecked ->
                // Kirim status perubahan CheckBox ke listener
                onTaskCheckedChangeListener(task, isChecked)
            }
        }

        //helper method formatTime utk mengonversi objek Calender ke format waktu yg mudah dibaca (HH:mm)
        private fun formatTime(calendar: Calendar?): String {
            if (calendar != null) {
                val format = SimpleDateFormat("HH:mm", Locale.getDefault())
                return format.format(calendar.time)
            }
            return ""
        }
    }
}