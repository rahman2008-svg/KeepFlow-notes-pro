package com.example.domain.model

data class Note(
    val id: Long = 0,
    val title: String = "",
    val content: String = "", // Used for text notes
    val isChecklist: Boolean = false,
    val checklistItems: List<ChecklistItem> = emptyList(),
    val isPinned: Boolean = false,
    val isArchived: Boolean = false,
    val isTrashed: Boolean = false,
    val colorHex: Long = 0xFFFFFFFF, // White by default, or dynamic Material color
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val reminderTime: Long? = null,
    val hasReminderOccurred: Boolean = false,
    val labelIds: List<Long> = emptyList()
)
