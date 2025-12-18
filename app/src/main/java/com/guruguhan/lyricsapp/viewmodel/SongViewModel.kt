package com.guruguhan.lyricsapp.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.guruguhan.lyricsapp.data.AppDatabase
import com.guruguhan.lyricsapp.data.Song
import com.guruguhan.lyricsapp.data.SongRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class SongViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: SongRepository

    val allSongs get() = repository.allSongs

    private val _errorEvents = MutableSharedFlow<String>()
    val errorEvents = _errorEvents.asSharedFlow()

    init {
        val songDao = AppDatabase.getDatabase(application).songDao()
        repository = SongRepository(songDao)
    }

    fun insert(song: Song) = viewModelScope.launch {
        try {
            repository.insert(song)
        } catch (e: Exception) {
            Log.e("SongViewModel", "Error inserting song: ${e.message}")
            _errorEvents.emit("Failed to add song: ${e.localizedMessage}")
        }
    }

    fun update(song: Song) = viewModelScope.launch {
        try {
            repository.update(song)
        } catch (e: Exception) {
            Log.e("SongViewModel", "Error updating song: ${e.message}")
            _errorEvents.emit("Failed to update song: ${e.localizedMessage}")
        }
    }

    fun delete(song: Song) = viewModelScope.launch {
        try {
            repository.delete(song)
        } catch (e: Exception) {
            Log.e("SongViewModel", "Error deleting song: ${e.message}")
            _errorEvents.emit("Failed to delete song: ${e.localizedMessage}")
        }
    }

    fun search(query: String) =
        repository.search(query)
}