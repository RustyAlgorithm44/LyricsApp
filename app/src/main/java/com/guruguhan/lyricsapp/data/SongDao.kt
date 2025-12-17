package com.guruguhan.lyricsapp.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SongDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(song: Song)

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
}