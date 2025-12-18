package com.guruguhan.lyricsapp

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import com.google.android.material.navigation.NavigationView
import com.guruguhan.lyricsapp.backup.BackupManager
import com.guruguhan.lyricsapp.data.AppDatabase
import kotlinx.coroutines.launch

class SettingsActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var toggle: ActionBarDrawerToggle

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

        drawerLayout = findViewById(R.id.drawer_layout)
        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener(this)

        toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        // Set hamburger icon color to white
        toggle.drawerArrowDrawable.color = Color.WHITE

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

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_settings -> {
                // Already in Settings
                drawerLayout.closeDrawer(GravityCompat.START)
            }
            R.id.nav_categories -> {
                Toast.makeText(this, "Categories coming soon!", Toast.LENGTH_SHORT).show()
            }
            R.id.nav_share -> {
                Toast.makeText(this, "Share coming soon!", Toast.LENGTH_SHORT).show()
            }
            R.id.nav_send -> {
                Toast.makeText(this, "Send coming soon!", Toast.LENGTH_SHORT).show()
            }
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
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