package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.NoteDatabase
import com.example.data.NoteRepository
import com.example.ui.Note10Screen
import com.example.ui.theme.Note10Theme
import com.example.viewmodel.Note10ViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = NoteDatabase.getInstance(applicationContext)
        val repository = NoteRepository(database.notebookDao(), database.resourceDao())

        setContent {
            Note10Theme {
                val vm: Note10ViewModel = viewModel(factory = Note10ViewModel.factory(repository))
                Note10Screen(viewModel = vm)
            }
        }
    }
}
