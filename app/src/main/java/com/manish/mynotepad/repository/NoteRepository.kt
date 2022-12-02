package com.manish.mynotepad.repository

import com.manish.mynotepad.database.NoteDatabase
import com.manish.mynotepad.model.Note

class NoteRepository(private val noteDatabase: NoteDatabase) {

    fun getAllNotes() = noteDatabase.getNoteDao().getAllNotes()
    fun searchNotes(query: String) = noteDatabase.getNoteDao().searchNote(query)
    suspend fun insertNote(note: Note) = noteDatabase.getNoteDao().insertNote(note)
    suspend fun deleteNote(note: Note) = noteDatabase.getNoteDao().deleteNote(note)
    suspend fun updateNote(note: Note) = noteDatabase.getNoteDao().updateNote(note)
}