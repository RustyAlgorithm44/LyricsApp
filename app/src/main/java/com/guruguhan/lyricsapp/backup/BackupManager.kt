package com.guruguhan.lyricsapp.backup

import android.content.Context
import com.guruguhan.lyricsapp.data.AppDatabase
import com.guruguhan.lyricsapp.data.Song
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object BackupManager {

    suspend fun exportSongsAsJson(context: Context): String {
        val dao = AppDatabase.getDatabase(context).songDao()
        val songs = dao.getAllSongsOnce()

        val jsonArray = JSONArray()

        for (song in songs) {
            val obj = JSONObject().apply {
                put("title", song.title)
                put("artist", song.artist)
                put("category", song.category)
                put("lyrics", song.lyrics)
                put("youtubeLink", song.youtubeLink)
            }
            jsonArray.put(obj)
        }

        return jsonArray.toString(2) // pretty JSON
    }

    suspend fun importSongs(context: Context, json: String) {
        val dao = AppDatabase.getDatabase(context).songDao()
        val array = JSONArray(json)

        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            val title = obj.getString("title")
            val artist = obj.getString("artist")
            val category = obj.getString("category")
            val lyrics = obj.getString("lyrics")
            val youtubeLink = obj.optString("youtubeLink", null)

            val existingSong = dao.findSongByTitleAndArtist(title, artist)
            if (existingSong == null) {
                dao.insert(
                    Song(
                        title = title,
                        artist = artist,
                        category = category,
                        lyrics = lyrics,
                        youtubeLink = youtubeLink
                    )
                )
            }
        }
    }
}