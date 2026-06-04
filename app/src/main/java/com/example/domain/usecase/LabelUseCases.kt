package com.example.domain.usecase

import com.example.domain.model.Label
import com.example.domain.repository.LabelRepository
import kotlinx.coroutines.flow.Flow

class GetAllLabelsUseCase(private val repository: LabelRepository) {
    operator fun invoke(): Flow<List<Label>> = repository.getAllLabels()
}

class SaveLabelUseCase(private val repository: LabelRepository) {
    suspend operator fun invoke(label: Label): Long = repository.insertLabel(label)
}

class DeleteLabelUseCase(private val repository: LabelRepository) {
    suspend operator fun invoke(label: Label) = repository.deleteLabel(label)
}

data class LabelUseCases(
    val getAllLabels: GetAllLabelsUseCase,
    val saveLabel: SaveLabelUseCase,
    val deleteLabel: DeleteLabelUseCase
)
