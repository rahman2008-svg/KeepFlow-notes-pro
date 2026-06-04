package com.example.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface LabelDao {
    @Query("SELECT * FROM labels ORDER BY name ASC")
    fun getAllLabels(): Flow<List<LabelEntity>>

    @Query("SELECT * FROM labels WHERE id = :id")
    suspend fun getLabelById(id: Long): LabelEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertLabel(label: LabelEntity): Long

    @Delete
    suspend fun deleteLabel(label: LabelEntity)

    @Query("SELECT * FROM labels WHERE name = :name LIMIT 1")
    suspend fun getLabelByName(name: String): LabelEntity?
}
