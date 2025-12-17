package com.guruguhan.lyricsapp.data

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue

@RunWith(AndroidJUnit4::class)
@SmallTest
class SongDaoTest {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: AppDatabase
    private lateinit var songDao: SongDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(
            context,
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        songDao = database.songDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun insertAndGetSong() = runBlocking {
        val song = Song(id = 1, title = "Test Song", artist = "Test Artist", category = "Pop", lyrics = "Test Lyrics", youtubeLink = "youtube.com/test")
        songDao.insert(song)

        val allSongs = songDao.getAllSongs().first()
        assertEquals(1, allSongs.size)
        assertEquals(song, allSongs[0])
    }

    @Test
    fun getAllSongsSortedByTitleAsc() = runBlocking {
        val song1 = Song(id = 1, title = "B Song", artist = "Artist A", category = "Pop", lyrics = "Lyrics B")
        val song2 = Song(id = 2, title = "A Song", artist = "Artist B", category = "Rock", lyrics = "Lyrics A")
        songDao.insert(song1)
        songDao.insert(song2)

        val sortedSongs = songDao.getAllSongs().first() // Use getAllSongs as it's already sorted by title ASC
        assertEquals(2, sortedSongs.size)
        assertEquals("A Song", sortedSongs[0].title)
        assertEquals("B Song", sortedSongs[1].title)
    }

    @Test
    fun getSongCount() = runBlocking {
        assertEquals(0, songDao.getSongCount())

        val song1 = Song(id = 1, title = "Song 1", artist = "Artist 1", category = "Pop", lyrics = "Lyrics 1")
        songDao.insert(song1)
        assertEquals(1, songDao.getSongCount())

        val song2 = Song(id = 2, title = "Song 2", artist = "Artist 2", category = "Rock", lyrics = "Lyrics 2")
        songDao.insert(song2)
        assertEquals(2, songDao.getSongCount())

        songDao.delete(song1)
        assertEquals(1, songDao.getSongCount())

        songDao.deleteAll()
        assertEquals(0, songDao.getSongCount())
    }

    @Test
    fun searchSongs() = runBlocking {
        val song1 = Song(id = 1, title = "Hello", artist = "Adele", category = "Pop", lyrics = "Hello from the other side")
        val song2 = Song(id = 2, title = "World", artist = "Various", category = "Misc", lyrics = "World of wonders")
        songDao.insert(song1)
        songDao.insert(song2)

        val searchResults = songDao.searchSongs("Hello").first()
        assertEquals(1, searchResults.size)
        assertEquals("Hello", searchResults[0].title)

        val searchResults2 = songDao.searchSongs("world").first() // Case-insensitive search
        assertEquals(1, searchResults2.size)
        assertEquals("World", searchResults2[0].title)

        val searchResults3 = songDao.searchSongs("nonexistent").first()
        assertTrue(searchResults3.isEmpty())
    }

    @Test
    fun deleteSong() = runBlocking {
        val song1 = Song(id = 1, title = "Song 1", artist = "Artist 1", category = "Pop", lyrics = "Lyrics 1")
        val song2 = Song(id = 2, title = "Song 2", artist = "Artist 2", category = "Rock", lyrics = "Lyrics 2")
        songDao.insert(song1)
        songDao.insert(song2)

        var allSongs = songDao.getAllSongs().first()
        assertEquals(2, allSongs.size)

        songDao.delete(song1)
        allSongs = songDao.getAllSongs().first()
        assertEquals(1, allSongs.size)
        assertEquals(song2, allSongs[0])
    }

    @Test
    fun updateSong() = runBlocking {
        val song = Song(id = 1, title = "Original Title", artist = "Original Artist", category = "Pop", lyrics = "Original Lyrics")
        songDao.insert(song)

        val updatedSong = song.copy(title = "Updated Title", lyrics = "Updated Lyrics", youtubeLink = "youtube.com/updated")
        songDao.update(updatedSong)

        val retrievedSong = songDao.getAllSongs().first()[0]
        assertEquals("Updated Title", retrievedSong.title)
        assertEquals("Updated Lyrics", retrievedSong.lyrics)
        assertEquals("youtube.com/updated", retrievedSong.youtubeLink)
    }

    @Test
    fun deleteAllSongs() = runBlocking {
        val song1 = Song(id = 1, title = "Song 1", artist = "Artist 1", category = "Pop", lyrics = "Lyrics 1")
        val song2 = Song(id = 2, title = "Song 2", artist = "Artist 2", category = "Rock", lyrics = "Lyrics 2")
        songDao.insert(song1)
        songDao.insert(song2)

        var allSongs = songDao.getAllSongs().first()
        assertEquals(2, allSongs.size)

        songDao.deleteAll()
        allSongs = songDao.getAllSongs().first()
        assertTrue(allSongs.isEmpty())
    }
}
