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
            val count = AppDatabase
                .getDatabase(this@SettingsActivity)
                .songDao()
                .getSongCount()

            findViewById<TextView>(R.id.songCountText)
                .text = "Total songs: $count"
        }

        // Export button
        findViewById<Button>(R.id.exportButton).setOnClickListener {
            lifecycleScope.launch {
                val file = BackupManager.exportSongs(this@SettingsActivity)
                Toast.makeText(
                    this@SettingsActivity,
                    "Exported to ${file.absolutePath}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        // Import button
        findViewById<Button>(R.id.importButton).setOnClickListener {
            importLauncher.launch(arrayOf("application/json"))
        }

        findViewById<TextView>(R.id.versionText)
            .text = "Version ${BuildConfig.VERSION_NAME}"
    }
}