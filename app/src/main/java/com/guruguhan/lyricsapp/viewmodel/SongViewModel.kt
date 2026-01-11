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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

class SongViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: SongRepository = SongRepository(AppDatabase.getDatabase(application).songDao())

    val allSongs get() = repository.allSongs
    val favoriteSongs get() = repository.favoriteSongs

    private val _searchQuery = MutableStateFlow<String?>(null)
    val searchQuery = _searchQuery.asStateFlow()

    private val _selectedSongs = MutableStateFlow<Set<Song>>(emptySet())
    val selectedSongs = _selectedSongs.asStateFlow()

    val isInActionMode = _selectedSongs.map { it.isNotEmpty() }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    private val _editCommand = MutableSharedFlow<Song>()
    val editCommand = _editCommand.asSharedFlow()

    fun requestEditForSelectedSong() {
        viewModelScope.launch {
            _selectedSongs.value.firstOrNull()?.let {
                _editCommand.emit(it)
            }
        }
    }

    fun toggleSongSelection(song: Song) {
        val currentSelection = _selectedSongs.value.toMutableSet()
        if (currentSelection.contains(song)) {
            currentSelection.remove(song)
        } else {
            currentSelection.add(song)
        }
        _selectedSongs.value = currentSelection
    }

    fun clearSelection() {
        _selectedSongs.value = emptySet()
    }

    fun deleteSelectedSongs() {
        _selectedSongs.value.forEach { delete(it) }
        clearSelection()
    }

    fun setSearchQuery(query: String?) {
        _searchQuery.value = query
    }

    val songsByDeity = allSongs.map { songs ->
        songs.groupBy { it.deity }
    }
    val songsByComposer = allSongs.map { songs ->
        songs.filter { !it.composer.isNullOrBlank() }.groupBy { it.composer!! }
    }
    val songsByCategory = allSongs.map { songs ->
        songs.flatMap { song ->
            song.categories.map { category -> category to song }
        }.groupBy({ it.first }, { it.second })
    }

    private val _errorEvents = MutableSharedFlow<String>()
    val errorEvents = _errorEvents.asSharedFlow()


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
    val uniqueCategories = repository.getUniqueCategories()

    fun getSongsByDeity(deity: String) = repository.getSongsByDeity(deity)
    fun getSongsByComposer(composer: String) = repository.getSongsByComposer(composer)
    fun getSongById(id: Int) = repository.getSongById(id)
}