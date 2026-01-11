package com.guruguhan.lyricsapp.data

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.LinkedHashMap

class DataTypeConverters {
    private val gson = Gson()

    @TypeConverter
    fun fromString(value: String): Map<String, String> {
        val mapType = object : TypeToken<LinkedHashMap<String, String>>() {}.type
        return gson.fromJson(value, mapType) ?: LinkedHashMap()
    }

    @TypeConverter
    fun fromMap(map: Map<String, String>): String {
        return gson.toJson(map)
    }

    @TypeConverter
    fun fromStringToList(value: String?): List<String> {
        if (value == null) {
            return emptyList()
        }
        val listType = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(value, listType)
    }

    @TypeConverter
    fun fromListToString(list: List<String>): String {
        return gson.toJson(list)
    }
}
