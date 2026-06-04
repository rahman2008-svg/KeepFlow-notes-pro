package com.example.data.repository

import com.example.data.local.LabelDao
import com.example.data.local.LabelEntity
import com.example.domain.model.Label
import com.example.domain.repository.LabelRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class LabelRepositoryImpl(private val labelDao: LabelDao) : LabelRepository {
    override fun getAllLabels(): Flow<List<Label>> {
        return labelDao.getAllLabels().map { list -> list.map { it.toDomain() } }
    }

    override suspend fun getLabelById(id: Long): Label? {
        return labelDao.getLabelById(id)?.toDomain()
    }

    override suspend fun insertLabel(label: Label): Long {
        return labelDao.insertLabel(LabelEntity.fromDomain(label))
    }

    override suspend fun deleteLabel(label: Label) {
        labelDao.deleteLabel(LabelEntity.fromDomain(label))
    }

    override suspend fun getLabelByName(name: String): Label? {
        return labelDao.getLabelByName(name)?.toDomain()
    }
}
