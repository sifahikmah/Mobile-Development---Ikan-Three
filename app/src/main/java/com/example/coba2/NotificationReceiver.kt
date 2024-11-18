package com.example.coba2

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat



class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val taskTitle = intent.getStringExtra("task_title")
        val taskId = intent.getIntExtra("task_id", -1)  // Defaultkan ke -1

        Log.d("NotificationReceiver", "taskTitle: $taskTitle, taskId: $taskId")  // Log untuk pengecekan

        if (!taskTitle.isNullOrEmpty() && taskId != -1) {
            createNotification(context, taskTitle, taskId)
        } else {
            Log.e("NotificationReceiver", "Task data missing or invalid")
        }
    }


    private fun createNotification(context: Context, taskTitle: String, taskId: Int) {
        val channelId = "task_reminder_channel"
        val channelName = "Task Reminder"

        // Mendapatkan NotificationManager
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Membuat NotificationChannel hanya jika API level 26 atau lebih tinggi
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(channel)
        }

        // Membuat NotificationCompat
        val notification = NotificationCompat.Builder(context, channelId)
            .setContentTitle("Task Reminder")
            .setContentText("Reminder for task: $taskTitle")
            .setSmallIcon(R.drawable.ic_launcher_foreground)  // Ganti dengan ikon yang sesuai
            .setAutoCancel(true)  // Menambahkan kemampuan untuk membatalkan notifikasi setelah diklik
            .build()

        // Menampilkan notifikasi dengan ID unik (taskId)
        notificationManager.notify(taskId, notification)  // Gunakan taskId untuk memastikan notifikasi unik
    }
}
