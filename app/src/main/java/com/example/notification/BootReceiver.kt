package com.example.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.data.local.AppDatabase
import com.example.data.preference.PreferenceManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        val pendingResult = goAsync()
        val database = AppDatabase.getDatabase(context)
        val taskDao = database.taskDao()
        val preferenceManager = PreferenceManager(context)
        val alarmScheduler = AlarmScheduler(context)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // 1. Recover daily reminder alarm
                val dailyEnabled = preferenceManager.dailyReminderEnabledFlow.first()
                if (dailyEnabled) {
                    val dailyTime = preferenceManager.dailyReminderTimeFlow.first() // default "08:00"
                    val parts = dailyTime.split(":")
                    if (parts.size == 2) {
                        val hour = parts[0].toIntOrNull() ?: 8
                        val min = parts[1].toIntOrNull() ?: 0
                        alarmScheduler.scheduleDailyReminder(hour, min)
                    }
                }

                // 2. Recover all upcoming task alarms
                val now = System.currentTimeMillis()
                val upcomingTasks = taskDao.getUpcomingTasks(now).first()
                for (task in upcomingTasks) {
                    if (task.reminderTime != null) {
                        alarmScheduler.scheduleTaskAlarm(task)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                pendingResult.finish()
            }
        }
    }
}
