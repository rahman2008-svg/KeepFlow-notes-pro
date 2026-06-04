package com.example.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.data.local.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Calendar

class DailyReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        val database = AppDatabase.getDatabase(context)
        val taskDao = database.taskDao()
        val notificationHelper = NotificationHelper(context)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val calendar = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                val startOfDay = calendar.timeInMillis
                
                calendar.apply {
                    set(Calendar.HOUR_OF_DAY, 23)
                    set(Calendar.MINUTE, 59)
                    set(Calendar.SECOND, 59)
                    set(Calendar.MILLISECOND, 999)
                }
                val endOfDay = calendar.timeInMillis

                // Retrieve tasks flow first emission synchronizing list
                val todayTasks = taskDao.getTasksForDateRange(startOfDay, endOfDay).first()
                val activeTasks = todayTasks.filter { !it.isCompleted }
                val highPriorityCount = activeTasks.count { it.priority.equals("High", ignoreCase = true) }

                notificationHelper.showDailySummaryNotification(
                    todayCount = activeTasks.size,
                    highPriorityCount = highPriorityCount
                )
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                pendingResult.finish()
            }
        }
    }
}
