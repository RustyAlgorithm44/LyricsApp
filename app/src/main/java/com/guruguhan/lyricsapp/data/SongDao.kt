package com.guruguhan.lyricsapp.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Delete
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface SongDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(song: Song)

    @Update
    suspend fun update(song: Song)

    @Delete
    suspend fun delete(song: Song)

    @Query("SELECT * FROM songs ORDER BY title ASC")
    fun getAllSongs(): Flow<List<Song>>

    @Query("""
    SELECT * FROM songs
    WHERE title LIKE '%' || :query || '%'
       OR artist LIKE '%' || :query || '%'
       OR lyrics LIKE '%' || :query || '%'
       OR category LIKE '%' || :query || '%'
    ORDER BY title ASC
""")
    fun searchSongs(query: String): kotlinx.coroutines.flow.Flow<List<Song>>

    @Query("SELECT COUNT(*) FROM songs")
    suspend fun getSongCount(): Int

    @Query("SELECT * FROM songs")
    suspend fun getAllSongsOnce(): List<Song>

    @Query("SELECT * FROM songs WHERE title = :title AND artist = :artist LIMIT 1")
    suspend fun findSongByTitleAndArtist(title: String, artist: String): Song?

    @Query("DELETE FROM songs")
    suspend fun deleteAll()
}