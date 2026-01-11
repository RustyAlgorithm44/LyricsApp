package com.guruguhan.lyricsapp

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import android.util.TypedValue
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.chip.ChipGroup
import com.guruguhan.lyricsapp.backup.BackupManager
import com.guruguhan.lyricsapp.data.AppDatabase
import com.guruguhan.lyricsapp.ui.ThemeHelper
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // Enable back button
        supportActionBar?.title = "Settings" // Ensure title is set

        val typedValue = TypedValue()
        theme.resolveAttribute(com.google.android.material.R.attr.colorOnPrimary, typedValue, true)
        val tintedDrawable = toolbar.navigationIcon?.mutate()
        tintedDrawable?.setTint(typedValue.data)
        toolbar.navigationIcon = tintedDrawable

        // Show song count
        lifecycleScope.launch {
            refreshSongCount()
        }

        setupTheme()
        
        // Export button
        findViewById<Button>(R.id.exportButton).setOnClickListener {
            val dateFormat = SimpleDateFormat("ddMMyyyy", Locale.getDefault())
            val date = dateFormat.format(Date())
            val fileName = "lyrics_backup_$date.json"
            exportLauncher.launch(fileName)
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

                        // Finish the activity and go back to the previous screen
                        finish()
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
        lifecycleScope.launch {
            refreshSongCount()
        }
    }

    private fun setupTheme() {
        val themeChipGroup = findViewById<ChipGroup>(R.id.themeChipGroup)

        // Set initial chip selection
        when (ThemeHelper.getTheme(this)) {
            ThemeHelper.LIGHT_MODE -> themeChipGroup.check(R.id.lightThemeChip)
            ThemeHelper.DARK_MODE -> themeChipGroup.check(R.id.darkThemeChip)
            else -> themeChipGroup.check(R.id.systemThemeChip)
        }

        themeChipGroup.setOnCheckedChangeListener { _, checkedId ->

            val theme = when (checkedId) {
                R.id.lightThemeChip -> ThemeHelper.LIGHT_MODE
                R.id.darkThemeChip -> ThemeHelper.DARK_MODE
                else -> ThemeHelper.SYSTEM_DEFAULT
            }
            ThemeHelper.setTheme(this, theme)
        }
    }
    
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
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