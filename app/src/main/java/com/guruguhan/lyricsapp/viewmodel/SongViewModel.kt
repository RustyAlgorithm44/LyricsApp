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

import kotlinx.coroutines.flow.map

class SongViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: SongRepository

    val allSongs get() = repository.allSongs
    val favoriteSongs get() = repository.favoriteSongs

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

    fun toggleFavoriteStatus(song: Song) = viewModelScope.launch {
        val updatedSong = song.copy(isFavorite = !song.isFavorite)
        update(updatedSong)
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

    val uniqueDeities = repository.getUniqueDeities()
    val uniqueComposers = repository.getUniqueComposers()

    fun getSongsByDeity(deity: String) = repository.getSongsByDeity(deity)
    fun getSongsByComposer(composer: String) = repository.getSongsByComposer(composer)
    fun getSongById(id: Int) = repository.getSongById(id)
}