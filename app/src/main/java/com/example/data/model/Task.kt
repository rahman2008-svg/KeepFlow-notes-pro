package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val dueDate: Long, // Midnight timestamp (Epoch MS) for clean calendar logic
    val reminderTime: Long? = null, // Exact Epoch MS for notification
    val category: String = "Others", // Work, Personal, Education, Health, etc.
    val priority: String = "Medium", // High, Medium, Low
    val isCompleted: Boolean = false,
    val completedAt: Long? = null,
    val createdBySystem: Boolean = false
)
