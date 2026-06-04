package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Task
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks ORDER BY dueDate ASC, priority DESC")
    fun getAllTasks(): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun getTaskById(id: Int): Task?

    @Query("SELECT * FROM tasks WHERE id = :id")
    fun getTaskByIdFlow(id: Int): Flow<Task?>

    @Query("SELECT * FROM tasks WHERE dueDate >= :startOfDay AND dueDate <= :endOfDay ORDER BY priority DESC")
    fun getTasksForDateRange(startOfDay: Long, endOfDay: Long): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE isCompleted = :isCompleted")
    fun getTasksByCompletion(isCompleted: Boolean): Flow<List<Task>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: Task): Long

    @Update
    suspend fun updateTask(task: Task)

    @Delete
    suspend fun deleteTask(task: Task)

    @Query("SELECT * FROM tasks WHERE isCompleted = 0 AND reminderTime >= :now ORDER BY reminderTime ASC")
    fun getUpcomingTasks(now: Long): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE isCompleted = 0 AND dueDate < :now ORDER BY dueDate DESC")
    fun getOverdueTasks(now: Long): Flow<List<Task>>
}
