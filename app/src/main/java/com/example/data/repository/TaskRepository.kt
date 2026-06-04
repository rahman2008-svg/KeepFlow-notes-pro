package com.example.data.repository

import com.example.data.local.TaskDao
import com.example.data.model.Task
import kotlinx.coroutines.flow.Flow

class TaskRepository(private val taskDao: TaskDao) {

    val allTasks: Flow<List<Task>> = taskDao.getAllTasks()

    fun getTasksByCompletion(isCompleted: Boolean): Flow<List<Task>> {
        return taskDao.getTasksByCompletion(isCompleted)
    }

    suspend fun getTaskById(id: Int): Task? {
        return taskDao.getTaskById(id)
    }

    fun getTaskByIdFlow(id: Int): Flow<Task?> {
        return taskDao.getTaskByIdFlow(id)
    }

    fun getTasksForDateRange(startOfDay: Long, endOfDay: Long): Flow<List<Task>> {
        return taskDao.getTasksForDateRange(startOfDay, endOfDay)
    }

    suspend fun insert(task: Task): Long {
        return taskDao.insertTask(task)
    }

    suspend fun update(task: Task) {
        taskDao.updateTask(task)
    }

    suspend fun delete(task: Task) {
        taskDao.deleteTask(task)
    }

    fun getUpcomingTasks(now: Long): Flow<List<Task>> {
        return taskDao.getUpcomingTasks(now)
    }

    fun getOverdueTasks(now: Long): Flow<List<Task>> {
        return taskDao.getOverdueTasks(now)
    }
}
