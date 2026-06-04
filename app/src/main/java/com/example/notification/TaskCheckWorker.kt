package com.example.notification

import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.MainActivity
import com.example.data.local.AppDatabase
import kotlinx.coroutines.flow.first
import java.util.Calendar

class TaskCheckWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val database = AppDatabase.getDatabase(applicationContext)
        val taskDao = database.taskDao()
        val alarmScheduler = AlarmScheduler(applicationContext)

        try {
            val now = System.currentTimeMillis()

            // 1. Recover and Reschedule upcoming task alarms (self-healing)
            val upcomingTasks = taskDao.getUpcomingTasks(now).first()
            for (task in upcomingTasks) {
                if (task.reminderTime != null) {
                    alarmScheduler.scheduleTaskAlarm(task)
                }
            }

            // 2. Identify Missed/Overdue Tasks and trigger Missed Task Alerts
            val overdueTasks = taskDao.getOverdueTasks(now).first()
            val activeOverdue = overdueTasks.filter { !it.isCompleted }

            if (activeOverdue.isNotEmpty()) {
                val notificationManager = NotificationManagerCompat.from(applicationContext)
                
                // Show a single consolidated missed tasks summary
                val clickIntent = android.content.Intent(applicationContext, MainActivity::class.java).apply {
                    flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                val clickPendingIntent = android.app.PendingIntent.getActivity(
                    applicationContext,
                    8888,
                    clickIntent,
                    android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
                )

                val builder = NotificationCompat.Builder(applicationContext, NotificationHelper.CHANNEL_TASK_ALERTS_ID)
                    .setSmallIcon(android.R.drawable.ic_dialog_alert)
                    .setContentTitle("Missed Tasks Alert ⚠️")
                    .setContentText("You have ${activeOverdue.size} overdue pending tasks that need your attention.")
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setContentIntent(clickPendingIntent)
                    .setAutoCancel(true)

                notificationManager.notify(8888, builder.build())
            }

            return Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            return Result.retry()
        }
    }
}
