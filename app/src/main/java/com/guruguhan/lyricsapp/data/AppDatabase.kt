package com.guruguhan.lyricsapp.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [Song::class],
    version = 6,
    exportSchema = false
)
@androidx.room.TypeConverters(MapTypeConverter::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun songDao(): SongDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE songs ADD COLUMN isFavorite INTEGER NOT NULL DEFAULT 0")
            }
        }

        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                val cursor = database.query("SELECT id, lyrics FROM songs")
                val gson = com.google.gson.Gson() // Declare Gson once
                while (cursor.moveToNext()) {
                    val id = cursor.getInt(cursor.getColumnIndexOrThrow("id"))
                    val lyrics = cursor.getString(cursor.getColumnIndexOrThrow("lyrics"))

                    // Create a map and convert to JSON
                    val lyricsMap = mapOf("Default" to lyrics)
                    val jsonLyrics = gson.toJson(lyricsMap) // Use the declared Gson instance

                    // Update the row
                    val contentValues = android.content.ContentValues().apply {
                        put("lyrics", jsonLyrics)
                    }
                    database.update("songs", android.database.sqlite.SQLiteDatabase.CONFLICT_NONE, contentValues, "id = ?", arrayOf(id.toString()))
                }
                cursor.close() // Close the cursor after use
            }
        }
        
        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE songs ADD COLUMN ragam TEXT DEFAULT NULL")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "lyrics_db"
                )
                .addMigrations(MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6)
                .build().also { INSTANCE = it }
            }
        }
    }
}