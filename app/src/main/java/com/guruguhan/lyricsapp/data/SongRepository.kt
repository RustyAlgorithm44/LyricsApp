package com.guruguhan.lyricsapp.data

import kotlinx.coroutines.flow.Flow

class SongRepository(private val songDao: SongDao) {

    val allSongs: Flow<List<Song>> = songDao.getAllSongs()

    suspend fun insert(song: Song) {
        songDao.insert(song)
    }
}