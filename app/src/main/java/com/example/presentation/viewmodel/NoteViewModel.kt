package com.example.presentation.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.domain.model.Label
import com.example.domain.model.Note
import com.example.domain.usecase.LabelUseCases
import com.example.domain.usecase.NoteUseCases
import com.example.utils.ReminderWorker
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class NoteViewModel(
    private val noteUseCases: NoteUseCases,
    private val labelUseCases: LabelUseCases
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _selectedLabelId = MutableStateFlow<Long?>(null)
    val selectedLabelId = _selectedLabelId.asStateFlow()

    private val _isGridView = MutableStateFlow(true)
    val isGridView = _isGridView.asStateFlow()

    private val _isArchivedView = MutableStateFlow(false)
    val isArchivedView = _isArchivedView.asStateFlow()

    private val _isTrashedView = MutableStateFlow(false)
    val isTrashedView = _isTrashedView.asStateFlow()

    val labels: StateFlow<List<Label>> = labelUseCases.getAllLabels()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val filteredNotes: StateFlow<List<Note>> = combine(
        noteUseCases.getAllNotes(),
        _searchQuery,
        _selectedLabelId,
        _isArchivedView,
        _isTrashedView
    ) { notes, query, labelId, isArchived, isTrashed ->
        notes.filter { note ->
            if (isTrashed) {
                note.isTrashed && (query.isEmpty() || 
                    note.title.contains(query, ignoreCase = true) || 
                    note.content.contains(query, ignoreCase = true))
            } else {
                !note.isTrashed &&
                note.isArchived == isArchived &&
                (labelId == null || note.labelIds.contains(labelId)) &&
                (query.isEmpty() || 
                    note.title.contains(query, ignoreCase = true) || 
                    note.content.contains(query, ignoreCase = true))
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectLabelFilter(labelId: Long?) {
        _selectedLabelId.value = labelId
    }

    fun toggleGridView() {
        _isGridView.value = !_isGridView.value
    }

    fun setArchivedView(showArchived: Boolean) {
        _isArchivedView.value = showArchived
        if (showArchived) {
            _isTrashedView.value = false
        }
    }

    fun setTrashedView(showTrashed: Boolean) {
        _isTrashedView.value = showTrashed
        if (showTrashed) {
            _isArchivedView.value = false
        }
    }

    // --- Note Management ---

    fun saveNote(note: Note, onComplete: ((Long) -> Unit)? = null) {
        viewModelScope.launch {
            val id = noteUseCases.saveNote(note)
            onComplete?.invoke(id)
        }
    }

    fun trashNote(note: Note) {
        viewModelScope.launch {
            // Unpin, unarchive and mark as trashed
            noteUseCases.saveNote(note.copy(isTrashed = true, isPinned = false, isArchived = false))
        }
    }

    fun restoreNote(note: Note) {
        viewModelScope.launch {
            noteUseCases.saveNote(note.copy(isTrashed = false))
        }
    }

    fun deleteNote(note: Note) {
        viewModelScope.launch {
            if (note.isTrashed) {
                noteUseCases.deleteNote(note)
            } else {
                trashNote(note)
            }
        }
    }

    fun deleteNoteById(id: Long) {
        viewModelScope.launch {
            val note = noteUseCases.getNote(id)
            if (note != null) {
                if (note.isTrashed) {
                    noteUseCases.deleteNote(id)
                } else {
                    trashNote(note)
                }
            }
        }
    }

    fun emptyTrash() {
        viewModelScope.launch {
            noteUseCases.getAllNotes().first().forEach { note ->
                if (note.isTrashed) {
                    noteUseCases.deleteNote(note)
                }
            }
        }
    }

    fun togglePinNote(note: Note) {
        viewModelScope.launch {
            noteUseCases.saveNote(note.copy(isPinned = !note.isPinned, isArchived = false))
        }
    }

    fun toggleArchiveNote(note: Note) {
        viewModelScope.launch {
            noteUseCases.saveNote(note.copy(isArchived = !note.isArchived, isPinned = false))
        }
    }

    fun updateNoteColor(note: Note, colorHex: Long) {
        viewModelScope.launch {
            noteUseCases.saveNote(note.copy(colorHex = colorHex))
        }
    }

    // --- Label Management ---

    fun createLabel(name: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            labelUseCases.saveLabel(Label(name = name))
        }
    }

    fun deleteLabel(label: Label) {
        viewModelScope.launch {
            // Delete the label master
            labelUseCases.deleteLabel(label)
            
            // Go through all notes and remove this label index statically
            noteUseCases.getAllNotes().first().forEach { note ->
                if (note.labelIds.contains(label.id)) {
                    val updatedIds = note.labelIds.filter { it != label.id }
                    noteUseCases.saveNote(note.copy(labelIds = updatedIds))
                }
            }
        }
    }

    // --- WorkManager Schedule Reminders ---

    fun scheduleReminder(context: Context, noteId: Long, reminderTimeMs: Long) {
        viewModelScope.launch {
            val note = noteUseCases.getNote(noteId) ?: return@launch
            val delayMs = reminderTimeMs - System.currentTimeMillis()
            
            val workRequest = OneTimeWorkRequestBuilder<ReminderWorker>()
                .setInitialDelay(maxOf(0, delayMs), TimeUnit.MILLISECONDS)
                .setInputData(workDataOf("NOTE_ID" to noteId))
                .build()

            WorkManager.getInstance(context.applicationContext).enqueueUniqueWork(
                "reminder_$noteId",
                ExistingWorkPolicy.REPLACE,
                workRequest
            )

            noteUseCases.saveNote(
                note.copy(
                    reminderTime = reminderTimeMs,
                    hasReminderOccurred = false
                )
            )
        }
    }

    fun cancelReminder(context: Context, noteId: Long) {
        viewModelScope.launch {
            val note = noteUseCases.getNote(noteId) ?: return@launch
            WorkManager.getInstance(context.applicationContext).cancelUniqueWork("reminder_$noteId")
            noteUseCases.saveNote(
                note.copy(
                    reminderTime = null,
                    hasReminderOccurred = false
                )
            )
        }
    }
}

@Suppress("UNCHECKED_CAST")
class NoteViewModelFactory(
    private val noteUseCases: NoteUseCases,
    private val labelUseCases: LabelUseCases
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NoteViewModel::class.java)) {
            return NoteViewModel(noteUseCases, labelUseCases) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
