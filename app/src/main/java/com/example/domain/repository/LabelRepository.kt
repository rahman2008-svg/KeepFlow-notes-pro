package com.example.domain.repository

import com.example.domain.model.Label
import kotlinx.coroutines.flow.Flow

interface LabelRepository {
    fun getAllLabels(): Flow<List<Label>>
    suspend fun getLabelById(id: Long): Label?
    suspend fun insertLabel(label: Label): Long
    suspend fun deleteLabel(label: Label)
    suspend fun getLabelByName(name: String): Label?
}
