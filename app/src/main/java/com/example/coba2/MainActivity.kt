package com.example.coba2

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.coba2.adapter.TaskAdapter
import com.example.coba2.model.Task
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {

    //recycleview utk menampilkan item yg diikat dg taskadapter
    private lateinit var recyclerView: RecyclerView
    //fab (floating action button) = tombol untuk menambahkan tugas baru
    private lateinit var fab: FloatingActionButton
    //Adapter ini digunakan untuk menghubungkan data tasks dengan tampilan RecyclerView
    private var taskAdapter: TaskAdapter? = null
    private var tasks = ArrayList<Task>()
    //mengatur alarm yang terkait dengan tugas, yang akan memberikan notifikasi kepada pengguna pada waktu yang telah ditentukan
    private lateinit var alarmManager: AlarmManager

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (!isGranted) {
            Toast.makeText(this, "Notification permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    //Daftar untuk menyimpan tugas yang telah selesai
    private var completedTasks = ArrayList<Task>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        checkAndRequestPermissions()

        recyclerView = findViewById(R.id.task_recycler_view)
        fab = findViewById(R.id.fab)

        // Menginisialisasi taskAdapter
        taskAdapter = TaskAdapter(
            tasks,
            onEditClickListener = { task -> onEditClick(task) },
            onDeleteClickListener = { task -> onDeleteClick(task) },
            onTaskCheckedChangeListener = { task, isChecked ->
                handleTaskCheckChange(task, isChecked)
            }
        )

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = taskAdapter

        fab.setOnClickListener {
            val intent = Intent(this, AddTaskActivity::class.java)
            startActivityForResult(intent, REQUEST_CODE_ADD_TASK)
        }
    }

    private fun handleTaskCheckChange(task: Task, isChecked: Boolean) {
        task.isChecked = isChecked

        // Update both task lists and notify adapter
        taskAdapter?.notifyDataSetChanged()
    }

    private fun updateTaskList() {
        // Perbarui taskAdapter dengan data tasks yang sudah terupdate
        taskAdapter = TaskAdapter(
            tasks,  // Tampilkan hanya tasks yang ada (aktif)
            onEditClickListener = { task -> onEditClick(task) },
            onDeleteClickListener = { task -> onDeleteClick(task) },
            onTaskCheckedChangeListener = { task, isChecked ->
                handleTaskCheckChange(task, isChecked)
            }
        )
        recyclerView.adapter = taskAdapter
    }


    private fun onEditClick(task: Task) {
        // Menangani aksi edit, membuka activity untuk mengedit task
        val intent = Intent(this, EditTaskActivity::class.java)
        intent.putExtra("task", task)
        startActivityForResult(intent, REQUEST_CODE_EDIT_TASK)
    }

    private fun onDeleteClick(task: Task) {
        // Menghapus task dari daftar
        tasks.remove(task)

        // Menunda pemanggilan notifyDataSetChanged() sampai layout selesai
        recyclerView.post {
            taskAdapter?.notifyDataSetChanged()  // Update adapter setelah data diubah
        }

        // Membatalkan alarm terkait task yang dihapus
        cancelNotification(task)

        Toast.makeText(this, "Task ${task.title} deleted", Toast.LENGTH_SHORT).show()
    }

    private fun cancelNotification(task: Task) {
        try {
            // Buat PendingIntent yang digunakan untuk alarm
            val intent = Intent(this, NotificationReceiver::class.java).apply {
                putExtra("task_title", task.title)
                putExtra("task_id", task.id.hashCode())
            }

            val pendingIntent = PendingIntent.getBroadcast(
                this,
                task.id.hashCode(),
                intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

            // Batalkan alarm menggunakan PendingIntent yang sama
            alarmManager.cancel(pendingIntent)

            Toast.makeText(this, "Notification canceled for ${task.title}", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e("MainActivity", "Error canceling notification", e)
            Toast.makeText(this, "Error canceling notification: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkAndRequestPermissions() {
        // Check for notification permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        // Periksa izin alarm yang tepat
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                // Redirect user ke settings utl mengaktifkan alarms yg tepat
                Intent().also { intent ->
                    intent.action = Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
                    startActivity(intent)
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        try {
            if (requestCode == REQUEST_CODE_ADD_TASK && resultCode == RESULT_OK) {
                val task = data?.getParcelableExtra<Task>("task")
                task?.let {
                    tasks.add(it)
                    taskAdapter?.notifyItemInserted(tasks.size - 1)
                    scheduleNotification(it)
                }
            }

            if (requestCode == REQUEST_CODE_EDIT_TASK && resultCode == RESULT_OK) {
                val updatedTask = data?.getParcelableExtra<Task>("updated_task")
                updatedTask?.let {
                    // Cari task yang sesuai dan perbarui
                    val index = tasks.indexOfFirst { it.id == updatedTask.id }
                    if (index != -1) {
                        tasks[index] = it
                        taskAdapter?.notifyItemChanged(index)
                        // Update notifikasi jika diperlukan
                        cancelNotification(tasks[index])  // Membatalkan notifikasi lama
                        scheduleNotification(updatedTask)  // Menjadwalkan notifikasi baru
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "Error in onActivityResult", e)
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun scheduleNotification(task: Task) {
        try {
            // Menambahkan log untuk memastikan data yang dikirim benar
            Log.d("MainActivity", "Scheduling notification for taskId: ${task.id.hashCode()} and title: ${task.title}")
            // Check apakah memiliki izin untuk menjadwalkan alarm yang tepat
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
                Toast.makeText(this, "Please allow exact alarms in settings", Toast.LENGTH_LONG).show()
                Intent().also { intent ->
                    intent.action = Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
                    startActivity(intent)
                }
                return
            }

            task.reminderTime?.let { calendar ->
                val intent = Intent(this, NotificationReceiver::class.java).apply {
                    putExtra("task_title", task.title)
                    putExtra("task_id", task.id.hashCode())  // Menggunakan hashCode dari ID atau UUID unik
                }

                val pendingIntent = PendingIntent.getBroadcast(
                    this,
                    task.id.hashCode(),
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )


                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        pendingIntent
                    )
                } else {
                    alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        pendingIntent
                    )
                }

                Toast.makeText(
                    this,
                    "Notification scheduled for ${task.title}",
                    Toast.LENGTH_SHORT
                ).show()
            } ?: run {
                Log.e("MainActivity", "ReminderTime is null")
                Toast.makeText(this, "Error: No reminder time set", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "Error scheduling notification", e)
            Toast.makeText(this, "Error scheduling notification: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        private const val REQUEST_CODE_ADD_TASK = 1
        private const val REQUEST_CODE_EDIT_TASK = 2
    }
}
