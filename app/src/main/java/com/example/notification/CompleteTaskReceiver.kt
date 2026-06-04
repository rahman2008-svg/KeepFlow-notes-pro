package com.example.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import com.example.data.local.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CompleteTaskReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != NotificationHelper.ACTION_MARK_COMPLETE) return
        val taskId = intent.getIntExtra(NotificationHelper.EXTRA_TASK_ID, -1)
        if (taskId == -1) return

        // Cancel notification immediately
        try {
            NotificationManagerCompat.from(context).cancel(taskId)
        } catch (e: SecurityException) {
            e.printStackTrace()
        }

        val pendingResult = goAsync()
        val database = AppDatabase.getDatabase(context)
        val taskDao = database.taskDao()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val task = taskDao.getTaskById(taskId)
                if (task != null) {
                    val updatedTask = task.copy(
                        isCompleted = true,
                        completedAt = System.currentTimeMillis()
                    )
                    taskDao.updateTask(updatedTask)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                pendingResult.finish()
            }
        }
    }
}
