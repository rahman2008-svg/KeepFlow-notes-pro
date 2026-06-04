package com.example.domain.usecase

import com.example.domain.model.Note
import com.example.domain.repository.NoteRepository
import kotlinx.coroutines.flow.Flow

class GetAllNotesUseCase(private val repository: NoteRepository) {
    operator fun invoke(): Flow<List<Note>> = repository.getAllNotes()
}

class GetNoteUseCase(private val repository: NoteRepository) {
    suspend operator fun invoke(id: Long): Note? = repository.getNoteById(id)
}

class SaveNoteUseCase(private val repository: NoteRepository) {
    suspend operator fun invoke(note: Note): Long = repository.insertNote(note)
}

class DeleteNoteUseCase(private val repository: NoteRepository) {
    suspend operator fun invoke(note: Note) = repository.deleteNote(note)
    suspend operator fun invoke(id: Long) = repository.deleteNoteById(id)
}

class SearchNotesUseCase(private val repository: NoteRepository) {
    operator fun invoke(query: String): Flow<List<Note>> = repository.searchNotes(query)
}

data class NoteUseCases(
    val getAllNotes: GetAllNotesUseCase,
    val getNote: GetNoteUseCase,
    val saveNote: SaveNoteUseCase,
    val deleteNote: DeleteNoteUseCase,
    val searchNotes: SearchNotesUseCase
)
