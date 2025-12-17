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

    suspend fun exportSongs(context: Context): File {
        val dao = AppDatabase.getDatabase(context).songDao()
        val songs = dao.getAllSongsOnce()

        val jsonArray = JSONArray()
        for (song in songs) {
            val obj = JSONObject().apply {
                put("title", song.title)
                put("artist", song.artist)
                put("category", song.category)
                put("lyrics", song.lyrics)
            }
            jsonArray.put(obj)
        }

        val sdf = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)
        val fileName = "lyrics_backup_${sdf.format(Date())}.json"
        val file = File(context.getExternalFilesDir(null), fileName)

        file.writeText(jsonArray.toString(2))
        return file
    }

    suspend fun importSongs(context: Context, json: String) {
        val dao = AppDatabase.getDatabase(context).songDao()
        val array = JSONArray(json)

        dao.deleteAll()

        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            dao.insert(
                Song(
                    title = obj.getString("title"),
                    artist = obj.getString("artist"),
                    category = obj.getString("category"),
                    lyrics = obj.getString("lyrics")
                )
            )
        }
    }
}