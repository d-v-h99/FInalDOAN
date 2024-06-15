package com.hoangdoviet.finaldoan

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val title = intent?.getStringExtra("EVENT_TITLE") ?: "No Title"
        val eventDate = intent?.getStringExtra("EVENT_DATE") ?: "No Date"

        val content = "Sự kiện $title sẽ xảy ra trong 15 phút nữa"
        showNotification(context, title, content, eventDate)
    }

    private fun showNotification(context: Context?, title: String, content: String, eventDate: String) {
        val channelId = "event_channel_id"
        val channelName = "Event Notifications"

        val notificationManager = context?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Tạo kênh thông báo cho Android Oreo trở lên
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val notificationChannel = NotificationChannel(channelId, channelName, importance)
            notificationManager.createNotificationChannel(notificationChannel)
        }

        // Tạo intent để mở MainActivity khi người dùng nhấn vào thông báo
        val activityIntent = Intent(context, MainActivity::class.java).apply {
            putExtra("EVENT_DATE", eventDate)
            putExtra("TARGET_FRAGMENT", "MonthFragment")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            eventDate.hashCode(),
            activityIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context!!, channelId)
            .setSmallIcon(R.drawable.ic_notification) // Đặt icon cho thông báo
            .setContentTitle("Thông báo sự kiện")
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
}