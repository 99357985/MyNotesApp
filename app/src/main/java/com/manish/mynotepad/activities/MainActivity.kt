package com.manish.mynotepad.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.ViewModelProvider
import com.manish.mynotepad.database.NoteDatabase
import com.manish.mynotepad.databinding.ActivityMainBinding
import com.manish.mynotepad.repository.NoteRepository
import com.manish.mynotepad.viewmodel.NoteViewModel
import com.manish.mynotepad.viewmodel.NoteViewModelFactory
import java.lang.Exception

class MainActivity : AppCompatActivity() {

    lateinit var noteViewModel: NoteViewModel
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        binding = ActivityMainBinding.inflate(layoutInflater)

        try {
            setContentView(binding.root)
            val noteRepository = NoteRepository(NoteDatabase(this))
            val noteViewModelFactory = NoteViewModelFactory(noteRepository)
            noteViewModel = ViewModelProvider(this, noteViewModelFactory)[NoteViewModel::class.java]
        } catch (e: Exception) {
            Log.d("TAG","Error")
        }
    }
}