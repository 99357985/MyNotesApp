package com.manish.mynotepad.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.manish.mynotepad.model.Note
import com.manish.mynotepad.repository.NoteRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NoteViewModel(private val repository: NoteRepository): ViewModel() {

    fun insertNote(newNote: Note) = viewModelScope.launch(Dispatchers.IO) {
        repository.insertNote(newNote)
    }
    fun updateNote(existingNote: Note) = viewModelScope.launch(Dispatchers.IO) {
        repository.updateNote(existingNote)
    }
    fun deleteNote(existingNote: Note) = viewModelScope.launch(Dispatchers.IO) {
        repository.deleteNote(existingNote)
    }
    fun searchNote(query: String): LiveData<List<Note>> {
        return repository.searchNotes(query)
    }

    fun getAllNotes(): LiveData<List<Note>> = repository.getAllNotes()
}