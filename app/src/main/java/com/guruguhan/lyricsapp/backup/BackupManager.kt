package com.guruguhan.lyricsapp.backup

import android.content.Context
import com.guruguhan.lyricsapp.data.AppDatabase
import com.guruguhan.lyricsapp.data.Song
import org.json.JSONArray
import org.json.JSONObject

object BackupManager {

    suspend fun exportSongsAsJson(context: Context): String {
        val dao = AppDatabase.getDatabase(context).songDao()
        val songs = dao.getAllSongsOnce()

        val jsonArray = JSONArray()

        for (song in songs) {
            val obj = JSONObject().apply {
                put("title", song.title)
                put("composer", song.composer)
                put("deity", song.deity)
                put("ragam", song.ragam)
                put("lyrics", JSONObject(song.lyrics as Map<*, *>))
                put("youtubeLink", song.youtubeLink)
                put("isFavorite", song.isFavorite)
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
            // Handle legacy "artist" key if "composer" is missing
            val composer = if (obj.has("composer")) {
                obj.getString("composer")
            } else {
                obj.optString("artist", "")
            }
            val deity = obj.optString("deity", null)
            val lyricsValue = obj.get("lyrics")
            val lyricsMap: Map<String, String> = if (lyricsValue is JSONObject) {
                lyricsValue.keys().asSequence().associateWith { key -> lyricsValue.getString(key) }
            } else {
                mapOf("Default" to lyricsValue.toString())
            }
            val youtubeLink = obj.optString("youtubeLink", null)
            val ragam = obj.optString("ragam", null)
            val isFavorite = obj.optBoolean("isFavorite", false)

            val existingSong = dao.findSongByTitleAndComposer(title, composer)
            if (existingSong == null) {
                dao.insert(
                    Song(
                        title = title,
                        composer = composer,
                        deity = deity,
                        lyrics = lyricsMap,
                        youtubeLink = youtubeLink,
                        ragam = ragam,
                        isFavorite = isFavorite
            }
        }
    }
}