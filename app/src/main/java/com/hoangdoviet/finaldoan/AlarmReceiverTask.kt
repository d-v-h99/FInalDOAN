package com.hoangdoviet.finaldoan

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.hoangdoviet.finaldoan.model.Task
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class AlarmReceiverTask : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d("AlarmReceiverTask", "onReceive called")
        val alarmType = intent?.getStringExtra("ALARM_TYPE") ?: return
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()

        Log.d("AlarmReceiverTask", "onReceive: Alarm type: $alarmType")

        when (alarmType) {
            "MorningAlarm" -> {
                Log.d("AlarmReceiverTask", "Handling Morning Alarm")
                handleMorningAlarm(context, db, userId)
            }
            "NightAlarm" -> {
                Log.d("AlarmReceiverTask", "Handling Night Alarm")
                handleNightAlarm(context, db, userId)
            }
        }


        setNextDayAlarm(context, alarmType)
    }

    private fun handleMorningAlarm(context: Context?, db: FirebaseFirestore, userId: String) {
        val todayDate = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
        val tasksByDateId = "$todayDate-$userId"
        val tasksByDateRef = db.collection("TasksByDate").document(tasksByDateId)

        tasksByDateRef.get().addOnSuccessListener { document ->
            if (document != null && document.exists()) {
                val taskIds = document.get("taskIds") as? List<String> ?: emptyList()
                if (taskIds.isNotEmpty()) {
                    val tasksCollection = db.collection("Tasks")
                    val batch = tasksCollection.whereIn(FieldPath.documentId(), taskIds)
                    batch.get().addOnSuccessListener { tasksSnapshot ->
                        val taskTitles = tasksSnapshot.documents.mapNotNull {
                            it.toObject(Task::class.java)?.title
                        }
                        val content = "Hôm nay bạn có các nhiệm vụ: ${taskTitles.joinToString(", ")}"
                        showNotification(context, "Nhắc nhở nhiệm vụ", content, todayDate)
                    }
                }
            }
        }
    }

    private fun handleNightAlarm(context: Context?, db: FirebaseFirestore, userId: String) {
        val todayDate = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
        val tasksByDateId = "$todayDate-$userId"
        val tasksByDateRef = db.collection("TasksByDate").document(tasksByDateId)

        tasksByDateRef.get().addOnSuccessListener { document ->
            if (document != null && document.exists()) {
                val taskIds = document.get("taskIds") as? List<String> ?: emptyList()
                if (taskIds.isNotEmpty()) {
                    val tasksCollection = db.collection("Tasks")
                    val batch = tasksCollection.whereIn(FieldPath.documentId(), taskIds)
                    batch.get().addOnSuccessListener { tasksSnapshot ->
                        val incompleteTasks = tasksSnapshot.documents.mapNotNull {
                            val task = it.toObject(Task::class.java)
                            if (task?.status != "Hoàn thành") task?.title else null
                        }
                        if (incompleteTasks.isNotEmpty()) {
                            val content = "Hôm nay bạn có các nhiệm vụ chưa hoàn thành: ${incompleteTasks.joinToString(", ")}"
                            showNotification(context, "Nhắc nhở nhiệm vụ", content, todayDate)
                        }
                    }
                }
            }
        }
    }

    private fun showNotification(context: Context?, title: String, content: String, date: String) {
        val channelId = "task_channel_id"
        val channelName = "Task Notifications"
        val notificationManager = context?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val notificationChannel = NotificationChannel(channelId, channelName, importance)
            notificationManager.createNotificationChannel(notificationChannel)
        }

        val activityIntent = Intent(context, MainActivity::class.java).apply {
            putExtra("EVENT_DATE", date)
            putExtra("TARGET_FRAGMENT", "TaskFragment")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            date.hashCode(),
            activityIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context!!, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }

    @SuppressLint("ScheduleExactAlarm")
    private fun setNextDayAlarm(context: Context?, alarmType: String) {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            add(Calendar.DAY_OF_YEAR, 1)
            when (alarmType) {
                "MorningAlarm" -> {
                    set(Calendar.HOUR_OF_DAY, 11)
                    set(Calendar.MINUTE, 32)
                }
                "NightAlarm" -> {
                    set(Calendar.HOUR_OF_DAY, 11)
                    set(Calendar.MINUTE, 33)
                }
            }
            set(Calendar.SECOND, 0)
        }

        val alarmManager = context?.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
        val alarmIntent = Intent(context, AlarmReceiverTask::class.java).apply {
            putExtra("ALARM_TYPE", alarmType)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarmType.hashCode(),
            alarmIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager?.setExact(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            pendingIntent
        )

        Log.d("AlarmReceiverTask", "Alarm set for next day: $alarmType at ${calendar.time}")
    }
}
