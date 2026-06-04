package com.example.domain.repository

import com.example.domain.model.Note
import kotlinx.coroutines.flow.Flow

interface NoteRepository {
    fun getAllNotes(): Flow<List<Note>>
    suspend fun getNoteById(id: Long): Note?
    suspend fun insertNote(note: Note): Long
    suspend fun deleteNote(note: Note)
    suspend fun deleteNoteById(id: Long)
    fun searchNotes(query: String): Flow<List<Note>>
    suspend fun getPendingReminders(): List<Note>
}
