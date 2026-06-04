package com.example.data.repository

import com.example.data.local.NoteDao
import com.example.data.local.NoteEntity
import com.example.domain.model.Note
import com.example.domain.repository.NoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class NoteRepositoryImpl(private val noteDao: NoteDao) : NoteRepository {
    override fun getAllNotes(): Flow<List<Note>> {
        return noteDao.getAllNotes().map { list -> list.map { it.toDomain() } }
    }

    override suspend fun getNoteById(id: Long): Note? {
        return noteDao.getNoteById(id)?.toDomain()
    }

    override suspend fun insertNote(note: Note): Long {
        return noteDao.insertNote(NoteEntity.fromDomain(note))
    }

    override suspend fun deleteNote(note: Note) {
        noteDao.deleteNote(NoteEntity.fromDomain(note))
    }

    override suspend fun deleteNoteById(id: Long) {
        noteDao.deleteNoteById(id)
    }

    override fun searchNotes(query: String): Flow<List<Note>> {
        return noteDao.searchNotes(query).map { list -> list.map { it.toDomain() } }
    }

    override suspend fun getPendingReminders(): List<Note> {
        return noteDao.getPendingReminders().map { it.toDomain() }
    }
}
