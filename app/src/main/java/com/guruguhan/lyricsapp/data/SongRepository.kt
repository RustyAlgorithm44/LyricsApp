package com.guruguhan.lyricsapp.data

import kotlinx.coroutines.flow.Flow

class SongRepository(private val songDao: SongDao) {

    val allSongs: Flow<List<Song>> = songDao.getAllSongs()
    val favoriteSongs: Flow<List<Song>> = songDao.getFavoriteSongs()

    suspend fun insert(song: Song) {
        songDao.insert(song)
    }

    suspend fun update(song: Song) {
        songDao.update(song)
    }

    suspend fun delete(song: Song) {
        songDao.delete(song)
    }

    fun search(query: String) =
        songDao.searchSongs(query)

    fun getUniqueDeities() = songDao.getUniqueDeities()

    fun getUniqueComposers() = songDao.getUniqueComposers()

    fun getSongsByDeity(deity: String) = songDao.getSongsByDeity(deity)

    fun getSongsByComposer(composer: String) = songDao.getSongsByComposer(composer)

    fun getSongById(id: Int) = songDao.getSongById(id)

}