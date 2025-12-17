package com.guruguhan.lyricsapp

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.guruguhan.lyricsapp.backup.BackupManager
import kotlinx.coroutines.launch
import com.guruguhan.lyricsapp.data.AppDatabase
import com.guruguhan.lyricsapp.BuildConfig

class SettingsActivity : AppCompatActivity() {

    private val exportLauncher =
        registerForActivityResult(
            androidx.activity.result.contract.ActivityResultContracts.CreateDocument("application/json")
        ) { uri ->
            if (uri != null) {
                lifecycleScope.launch {
                    val json = BackupManager.exportSongsAsJson(this@SettingsActivity)
                    contentResolver.openOutputStream(uri)?.use {
                        it.write(json.toByteArray())
                    }
                    Toast.makeText(this@SettingsActivity, "Export successful", Toast.LENGTH_LONG).show()
                }
            }
        }

    private val importLauncher =
        registerForActivityResult(
            androidx.activity.result.contract.ActivityResultContracts.OpenDocument()
        ) { uri ->
            if (uri != null) {
                lifecycleScope.launch {
                    val json = contentResolver
                        .openInputStream(uri)
                        ?.bufferedReader()
                        ?.use { it.readText() }

                    if (json != null) {
                        BackupManager.importSongs(this@SettingsActivity, json)
                        refreshSongCount()
                        Toast.makeText(
                            this@SettingsActivity,
                            "Import successful",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        // Show song count
        lifecycleScope.launch {
            refreshSongCount()
        }

        // Export button
        findViewById<Button>(R.id.exportButton).setOnClickListener {
            exportLauncher.launch("lyrics_backup.json")
        }

        // Import button
        findViewById<Button>(R.id.importButton).setOnClickListener {
            importLauncher.launch(arrayOf("application/json"))
        }

        findViewById<TextView>(R.id.versionText)
            .text = "Version ${BuildConfig.VERSION_NAME}"

        // Delete button
        findViewById<Button>(R.id.deleteAllButton).setOnClickListener {

            androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Delete all songs?")
                .setMessage("This action cannot be undone.")
                .setPositiveButton("Delete") { _, _ ->
                    lifecycleScope.launch {
                        AppDatabase
                            .getDatabase(this@SettingsActivity)
                            .songDao()
                            .deleteAll()

                        refreshSongCount()

                        Toast.makeText(
                            this@SettingsActivity,
                            "All songs deleted",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
        lifecycleScope.launch {
            refreshSongCount()
        }
    }

    private suspend fun refreshSongCount() {
        val count = AppDatabase
            .getDatabase(this@SettingsActivity)
            .songDao()
            .getSongCount()

        findViewById<TextView>(R.id.songCountText)
            .text = "Total songs: $count"
    }
}