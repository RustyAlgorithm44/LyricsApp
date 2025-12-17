package com.guruguhan.lyricsapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.guruguhan.lyricsapp.data.AppDatabase
import com.guruguhan.lyricsapp.data.Song
import com.guruguhan.lyricsapp.data.SongRepository
import kotlinx.coroutines.launch

class SongViewModel(application: Application) : AndroidViewModel(application) {

    private lateinit var repository: SongRepository
    val allSongs = AppDatabase
        .getDatabase(application)
        .songDao()
        .getAllSongs()

    init {
        val songDao = AppDatabase.getDatabase(application).songDao()
        repository = SongRepository(songDao)
    }

    fun insert(song: Song) = viewModelScope.launch {
        repository.insert(song)
    }
}