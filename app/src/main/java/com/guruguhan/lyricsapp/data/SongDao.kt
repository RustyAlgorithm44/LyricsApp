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
       OR composer LIKE '%' || :query || '%'
       OR deity LIKE '%' || :query || '%'
       OR lyrics LIKE '%' || :query || '%'
       OR category LIKE '%' || :query || '%'
    ORDER BY title ASC
""")
    fun searchSongs(query: String): kotlinx.coroutines.flow.Flow<List<Song>>

    @Query("SELECT COUNT(*) FROM songs")
    suspend fun getSongCount(): Int

    @Query("SELECT * FROM songs")
    suspend fun getAllSongsOnce(): List<Song>

    @Query("SELECT * FROM songs WHERE title = :title AND composer = :composer LIMIT 1")
    suspend fun findSongByTitleAndComposer(title: String, composer: String): Song?

    @Query("DELETE FROM songs")
    suspend fun deleteAll()
}