package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.domain.model.ChecklistItem
import com.example.domain.model.Note

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val content: String,
    val isChecklist: Boolean,
    val checklistItems: List<ChecklistItem>,
    val isPinned: Boolean,
    val isArchived: Boolean,
    val isTrashed: Boolean,
    val colorHex: Long,
    val createdAt: Long,
    val updatedAt: Long,
    val reminderTime: Long?,
    val hasReminderOccurred: Boolean,
    val labelIds: List<Long>
) {
    fun toDomain(): Note {
        return Note(
            id = id,
            title = title,
            content = content,
            isChecklist = isChecklist,
            checklistItems = checklistItems,
            isPinned = isPinned,
            isArchived = isArchived,
            isTrashed = isTrashed,
            colorHex = colorHex,
            createdAt = createdAt,
            updatedAt = updatedAt,
            reminderTime = reminderTime,
            hasReminderOccurred = hasReminderOccurred,
            labelIds = labelIds
        )
    }

    companion object {
        fun fromDomain(note: Note): NoteEntity {
            return NoteEntity(
                id = note.id,
                title = note.title,
                content = note.content,
                isChecklist = note.isChecklist,
                checklistItems = note.checklistItems,
                isPinned = note.isPinned,
                isArchived = note.isArchived,
                isTrashed = note.isTrashed,
                colorHex = note.colorHex,
                createdAt = note.createdAt,
                updatedAt = note.updatedAt,
                reminderTime = note.reminderTime,
                hasReminderOccurred = note.hasReminderOccurred,
                labelIds = note.labelIds
            )
        }
    }
}
