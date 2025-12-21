package com.guruguhan.lyricsapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "songs")
data class Song(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val composer: String,
    val deity: String? = null,
    val lyrics: String,
    val youtubeLink: String? = null,
    val isFavorite: Boolean = false
)