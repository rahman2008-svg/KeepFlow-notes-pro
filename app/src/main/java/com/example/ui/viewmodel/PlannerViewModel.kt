package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.data.local.AppDatabase
import com.example.data.model.Task
import com.example.data.preference.PreferenceManager
import com.example.data.repository.TaskRepository
import com.example.notification.AlarmScheduler
import com.example.notification.TaskCheckWorker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.concurrent.TimeUnit

class PlannerViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: TaskRepository
    private val preferenceManager: PreferenceManager
    private val alarmScheduler: AlarmScheduler

    init {
        val database = AppDatabase.getDatabase(application)
        repository = TaskRepository(database.taskDao())
        preferenceManager = PreferenceManager(application)
        alarmScheduler = AlarmScheduler(application)

        // Set up the background WorkManager task checker (self-healing + missed tasks alerts)
        setupBackgroundChecks(application)
        
        // Load default daily reminder setup
        setupDefaultDailyReminder()
    }

    private fun setupBackgroundChecks(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .build()

        val periodicRequest = PeriodicWorkRequestBuilder<TaskCheckWorker>(
            1, TimeUnit.HOURS // checks hourly
        )
        .setConstraints(constraints)
        .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "TaskPeriodicCheck",
            ExistingPeriodicWorkPolicy.KEEP,
            periodicRequest
        )
    }

    private fun setupDefaultDailyReminder() {
        viewModelScope.launch {
            try {
                // Get time preference or register 8:00 AM alarm
                val hourMin = preferenceManager.dailyReminderTimeFlow.first()
                val parts = hourMin.split(":")
                val h = parts.getOrNull(0)?.toIntOrNull() ?: 8
                val m = parts.getOrNull(1)?.toIntOrNull() ?: 0
                val enabled = preferenceManager.dailyReminderEnabledFlow.first()
                if (enabled) {
                    alarmScheduler.scheduleDailyReminder(h, m)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Settings flows
    val darkModeState: StateFlow<String> = preferenceManager.darkModeFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "system")

    val notificationsEnabledState: StateFlow<Boolean> = preferenceManager.notificationsEnabledFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val dailyReminderEnabledState: StateFlow<Boolean> = preferenceManager.dailyReminderEnabledFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val dailyReminderTimeState: StateFlow<String> = preferenceManager.dailyReminderTimeFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "08:00")

    fun setDarkMode(mode: String) {
        viewModelScope.launch { preferenceManager.setDarkMode(mode) }
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch { preferenceManager.setNotificationsEnabled(enabled) }
    }

    fun setDailyReminderEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferenceManager.setDailyReminderEnabled(enabled)
            if (enabled) {
                val time = dailyReminderTimeState.value
                val parts = time.split(":")
                val h = parts.getOrNull(0)?.toIntOrNull() ?: 8
                val m = parts.getOrNull(1)?.toIntOrNull() ?: 0
                alarmScheduler.scheduleDailyReminder(h, m)
            }
        }
    }

    fun setDailyReminderTime(hour: Int, minute: Int) {
        viewModelScope.launch {
            val formatted = String.format("%02d:%02d", hour, minute)
            preferenceManager.setDailyReminderTime(formatted)
            if (dailyReminderEnabledState.value) {
                alarmScheduler.scheduleDailyReminder(hour, minute)
            }
        }
    }

    // Task flows
    val allTasks: StateFlow<List<Task>> = repository.allTasks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // UI state for adding/editing a task
    private val _editingTask = MutableStateFlow<Task?>(null)
    val editingTask = _editingTask.asStateFlow()

    // Active calendar date filter (at midnight)
    private val _selectedDate = MutableStateFlow(getStartOfDayToday())
    val selectedDate = _selectedDate.asStateFlow()

    fun selectDate(timestamp: Long) {
        _selectedDate.value = getStartOfDay(timestamp)
    }

    // Tasks for the selected calendar date
    val selectedDateTasks: StateFlow<List<Task>> = combine(allTasks, selectedDate) { list, date ->
        list.filter { getStartOfDay(it.dueDate) == date }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Dashboard metrics & stats
    val todayTasks: StateFlow<List<Task>> = allTasks.map { list ->
        val today = getStartOfDayToday()
        list.filter { getStartOfDay(it.dueDate) == today }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val upcomingTasks: StateFlow<List<Task>> = allTasks.map { list ->
        val today = getStartOfDayToday()
        list.filter { !it.isCompleted && getStartOfDay(it.dueDate) > today }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val completedTasks: StateFlow<List<Task>> = allTasks.map { list ->
        list.filter { it.isCompleted }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val productivityScore: StateFlow<Int> = allTasks.map { list ->
        if (list.isEmpty()) 0
        else {
            val completed = list.count { it.isCompleted }
            ((completed.toFloat() / list.size) * 100).toInt()
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val weeklySummary: StateFlow<Map<Int, Int>> = allTasks.map { list ->
        // Return a map of Day of Week (Calendar.MONDAY..Calendar.SUNDAY) to number of completed tasks
        val completed = list.filter { it.isCompleted && it.completedAt != null }
        val map = mutableMapOf<Int, Int>()
        for (i in Calendar.SUNDAY..Calendar.SATURDAY) {
            map[i] = 0
        }
        val cal = Calendar.getInstance()
        completed.forEach { task ->
            cal.timeInMillis = task.completedAt ?: 0
            val day = cal.get(Calendar.DAY_OF_WEEK)
            map[day] = (map[day] ?: 0) + 1
        }
        map
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    // CRUDS
    fun saveTask(
        title: String,
        description: String,
        dueDate: Long,
        category: String,
        priority: String,
        reminderTime: Long?
    ) {
        viewModelScope.launch {
            val taskToSave = _editingTask.value
            if (taskToSave != null) {
                // Cancel old alarm in case time changed
                alarmScheduler.cancelTaskAlarm(taskToSave)
                
                val updated = taskToSave.copy(
                    title = title,
                    description = description,
                    dueDate = getStartOfDay(dueDate),
                    category = category,
                    priority = priority,
                    reminderTime = reminderTime
                )
                repository.update(updated)
                if (reminderTime != null && !updated.isCompleted) {
                    alarmScheduler.scheduleTaskAlarm(updated)
                }
            } else {
                val newId = repository.insert(
                    Task(
                        title = title,
                        description = description,
                        dueDate = getStartOfDay(dueDate),
                        category = category,
                        priority = priority,
                        reminderTime = reminderTime
                    )
                ).toInt()
                
                val createdTask = Task(
                    id = newId,
                    title = title,
                    description = description,
                    dueDate = getStartOfDay(dueDate),
                    category = category,
                    priority = priority,
                    reminderTime = reminderTime
                )
                if (reminderTime != null) {
                    alarmScheduler.scheduleTaskAlarm(createdTask)
                }
            }
            _editingTask.value = null // reset editor state
        }
    }

    fun startEditing(task: Task?) {
        _editingTask.value = task
    }

    fun stopEditing() {
        _editingTask.value = null
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            alarmScheduler.cancelTaskAlarm(task)
            repository.delete(task)
        }
    }

    fun toggleTaskCompletion(task: Task) {
        viewModelScope.launch {
            val updated = task.copy(
                isCompleted = !task.isCompleted,
                completedAt = if (!task.isCompleted) System.currentTimeMillis() else null
            )
            repository.update(updated)
            if (updated.isCompleted) {
                alarmScheduler.cancelTaskAlarm(updated)
            } else if (updated.reminderTime != null) {
                alarmScheduler.scheduleTaskAlarm(updated)
            }
        }
    }

    // Seed Initial mock data on first launch to ensure absolutely zero boilerplate empty experience (fully localized but beautiful data)
    fun seedSampleTasksIfEmpty() {
        viewModelScope.launch {
            val list = repository.allTasks.first()
            if (list.isEmpty()) {
                val today = getStartOfDayToday()
                val tomorrow = today + 24 * 60 * 60 * 1000
                val nextDay = tomorrow + 24 * 60 * 60 * 1000

                val samples = listOf(
                    Task(
                        title = "Welcome to Smart Day Planner X! 🚀",
                        description = "Your offline-first Material 3 task manager. Tap task cards to edit, or tap checkbox to complete them. Toggle and slide inside calendar to plan the week.",
                        dueDate = today,
                        category = "Others",
                        priority = "High",
                        isCompleted = false,
                        createdBySystem = true
                    ),
                    Task(
                        title = "Review Weekly Achievements",
                        description = "Take 10 minutes to analyze productivity charts and review completed goals.",
                        dueDate = today,
                        category = "Work",
                        priority = "Medium",
                        isCompleted = true,
                        completedAt = System.currentTimeMillis() - 7200000,
                        createdBySystem = true
                    ),
                    Task(
                        title = "Morning Gym Session 🏃‍♂️",
                        description = "Focus on cardio and stretching exercises.",
                        dueDate = today,
                        category = "Health",
                        priority = "High",
                        isCompleted = true,
                        completedAt = System.currentTimeMillis() - 15000000,
                        createdBySystem = true
                    ),
                    Task(
                        title = "Plan study session on AI UI design",
                        description = "Read clean design and spacing guidelines details.",
                        dueDate = tomorrow,
                        category = "Education",
                        priority = "Low",
                        isCompleted = false,
                        createdBySystem = true
                    ),
                    Task(
                        title = "Launch NexVora OS Product Update",
                        description = "Check next-gen features, responsive interfaces, and test tags integrations on Android Studio.",
                        dueDate = nextDay,
                        category = "Work",
                        priority = "High",
                        isCompleted = false,
                        createdBySystem = true
                    )
                )
                for (task in samples) {
                    repository.insert(task)
                }
            }
        }
    }

    // Helper functions for date operations
    private fun getStartOfDayToday(): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    private fun getStartOfDay(timestamp: Long): Long {
        val cal = Calendar.getInstance()
        cal.timeInMillis = timestamp
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }
}
