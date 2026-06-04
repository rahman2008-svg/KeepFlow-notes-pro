package com.example.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import com.example.R
import com.example.data.model.Task

class NotificationHelper(private val context: Context) {

    companion object {
        const val CHANNEL_TASK_ALERTS_ID = "channel_task_alerts"
        const val CHANNEL_DAILY_REMINDERS_ID = "channel_daily_reminders"
        
        const val EXTRA_TASK_ID = "extra_task_id"
        const val ACTION_MARK_COMPLETE = "com.example.notification.ACTION_MARK_COMPLETE"
        const val ACTION_SNOOZE = "com.example.notification.ACTION_SNOOZE"
    }

    init {
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Task Alerts Channel
            val taskChannel = NotificationChannel(
                CHANNEL_TASK_ALERTS_ID,
                "Task Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Critical alerts and reminders for your scheduled tasks"
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(taskChannel)

            // Daily Planner Digest Channel
            val dailyChannel = NotificationChannel(
                CHANNEL_DAILY_REMINDERS_ID,
                "Daily Digests",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Daily summaries and planner morning digests"
            }
            notificationManager.createNotificationChannel(dailyChannel)
        }
    }

    fun showTaskNotification(task: Task) {
        val clickIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra(EXTRA_TASK_ID, task.id)
        }
        
        // Use unique requestCodes for safety with Alarm intents
        val clickPendingIntent = PendingIntent.getActivity(
            context,
            task.id,
            clickIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Mark Completed Action
        val completeIntent = Intent(context, CompleteTaskReceiver::class.java).apply {
            action = ACTION_MARK_COMPLETE
            putExtra(EXTRA_TASK_ID, task.id)
        }
        val completePendingIntent = PendingIntent.getBroadcast(
            context,
            task.id + 100000,
            completeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Snooze Action (Snoozes for 15 minutes)
        val snoozeIntent = Intent(context, SnoozeTaskReceiver::class.java).apply {
            action = ACTION_SNOOZE
            putExtra(EXTRA_TASK_ID, task.id)
        }
        val snoozePendingIntent = PendingIntent.getBroadcast(
            context,
            task.id + 200000,
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_TASK_ALERTS_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm) // Safe system fallback icon
            .setContentTitle("Task Reminder: ${task.title}")
            .setContentText(task.description.ifBlank { "Priority: ${task.priority}" })
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setContentIntent(clickPendingIntent)
            .setAutoCancel(true)
            .addAction(android.R.drawable.checkbox_on_background, "Mark Done", completePendingIntent)
            .addAction(android.R.drawable.ic_menu_recent_history, "Snooze (15m)", snoozePendingIntent)

        try {
            // Under Android 13 (API 33)+, must check post notification permissions.
            // Let's rely on standard try-catch or framework checks.
            val notificationManager = NotificationManagerCompat.from(context)
            notificationManager.notify(task.id, builder.build())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun showDailySummaryNotification(todayCount: Int, highPriorityCount: Int) {
        val clickIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val clickPendingIntent = PendingIntent.getActivity(
            context,
            9999,
            clickIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val message = if (todayCount > 0) {
            "You have $todayCount tasks scheduled for today, including $highPriorityCount high priority ones!"
        } else {
            "No tasks scheduled for today! Time to plan your day."
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_DAILY_REMINDERS_ID)
            .setSmallIcon(android.R.drawable.ic_menu_today)
            .setContentTitle("Your Day At A Glance ☀️")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(clickPendingIntent)
            .setAutoCancel(true)

        try {
            val notificationManager = NotificationManagerCompat.from(context)
            notificationManager.notify(9999, builder.build())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
