package com.example.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.data.local.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TaskReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val taskId = intent.getIntExtra(NotificationHelper.EXTRA_TASK_ID, -1)
        if (taskId == -1) return

        val pendingResult = goAsync()
        val database = AppDatabase.getDatabase(context)
        val taskDao = database.taskDao()
        val notificationHelper = NotificationHelper(context)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val task = taskDao.getTaskById(taskId)
                if (task != null && !task.isCompleted) {
                    notificationHelper.showTaskNotification(task)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                pendingResult.finish()
            }
        }
    }
}
