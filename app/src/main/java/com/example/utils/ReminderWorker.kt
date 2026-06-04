package com.example.utils

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.data.local.NoteDatabase

class ReminderWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val noteId = inputData.getLong("NOTE_ID", -1L)
        if (noteId == -1L) return Result.failure()

        val db = NoteDatabase.getDatabase(applicationContext)
        val noteDao = db.noteDao()

        val note = noteDao.getNoteById(noteId)
        if (note != null && !note.hasReminderOccurred) {
            // Display notification using NotificationHelper
            val helper = NotificationHelper(applicationContext)
            
            val displayContent = if (note.isChecklist) {
                note.checklistItems.joinToString(", ") { 
                    (if (it.isChecked) "☑" else "☐") + " " + it.text 
                }
            } else {
                note.content
            }

            helper.showReminderNotification(note.id, note.title, displayContent)

            // Update database state to mark reminder as run
            val updatedNote = note.copy(hasReminderOccurred = true)
            noteDao.insertNote(updatedNote)
        }

        return Result.success()
    }
}
