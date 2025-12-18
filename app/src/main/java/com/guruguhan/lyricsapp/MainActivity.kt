package com.guruguhan.lyricsapp

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.navigation.NavigationView
import com.guruguhan.lyricsapp.ui.SongAdapter
import com.guruguhan.lyricsapp.viewmodel.SongViewModel
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var adapter: SongAdapter
    private val viewModel: SongViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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

        adapter = SongAdapter(
            onItemClick = { song ->
                // onItemClick is now handled within the SongAdapter to open SongDetailActivity
                // No action needed here as the adapter itself will start the activity.
            },
            onItemLongClick = { song ->
                if (!isInActionMode) {
                    startManualActionMode(song)
                }
            }
        )

        val recyclerView =
            findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.songRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        // 🔑 Collect Flow properly
        lifecycleScope.launch {
            viewModel.allSongs.collect { songs ->
                adapter.submitList(songs)
                if (songs.isEmpty()) {
                    // Show empty state message
                    findViewById<android.widget.TextView>(R.id.emptyStateTextView).visibility =
                        android.view.View.VISIBLE
                } else {
                    findViewById<android.widget.TextView>(R.id.emptyStateTextView).visibility =
                        android.view.View.GONE
                }
            }
        }

        val fab =
            findViewById<com.google.android.material.floatingactionbutton.FloatingActionButton>(
                R.id.addSongFab
            )

        fab.setOnClickListener {
            showAddSongDialog()
        }

        val searchInput = findViewById<android.widget.EditText>(R.id.searchInput)

        searchInput.addTextChangedListener(object : android.text.TextWatcher {
            override fun afterTextChanged(s: android.text.Editable?) {
                lifecycleScope.launch {
                    viewModel.search(s.toString()).collect { songs ->
                        adapter.submitList(songs)
                        if (songs.isEmpty() && !s.isNullOrBlank()) {
                            // Show no search results message
                            findViewById<android.widget.TextView>(R.id.noSearchResultsTextView).visibility =
                                android.view.View.VISIBLE
                        } else {
                            findViewById<android.widget.TextView>(R.id.noSearchResultsTextView).visibility =
                                android.view.View.GONE
                        }
                    }
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_settings -> {
                val intent = Intent(this, SettingsActivity::class.java)
                startActivity(intent)
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
            if (isInActionMode) {
                endManualActionMode()
            } else {
                super.onBackPressed()
            }
        }
    }

    private var selectedSong: com.guruguhan.lyricsapp.data.Song? = null
    private var isInActionMode = false

    private fun startManualActionMode(song: com.guruguhan.lyricsapp.data.Song) {
        isInActionMode = true
        selectedSong = song
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        toolbar.menu.clear()
        toolbar.inflateMenu(R.menu.menu_contextual_action_mode)
        drawerLayout.removeDrawerListener(toggle) // Remove toggle
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // Show back button
        toolbar.setNavigationIcon(R.drawable.ic_close)
        toolbar.navigationIcon?.setTint(Color.WHITE)
        toolbar.setNavigationOnClickListener {
            endManualActionMode()
        }
        toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.action_edit_song -> {
                    selectedSong?.let { songToEdit -> showEditSongDialog(songToEdit) }
                    endManualActionMode()
                    true
                }
                R.id.action_delete_song -> {
                    selectedSong?.let { songToDelete -> showDeleteConfirmationDialog(songToDelete) }
                    true
                }
                else -> false
            }
        }
    }

    private fun endManualActionMode() {
        isInActionMode = false
        selectedSong = null
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        toolbar.menu.clear()
        toolbar.inflateMenu(R.menu.menu_main)
        supportActionBar?.setDisplayHomeAsUpEnabled(false) // Hide back button
        drawerLayout.addDrawerListener(toggle) // Re-add toggle
        toggle.syncState() // Sync the state to show the hamburger icon
        toggle.drawerArrowDrawable.color = Color.WHITE // Re-set hamburger icon color to white
        toolbar.setNavigationOnClickListener(toggle.toolbarNavigationClickListener) // Restore toggle's listener
        toolbar.setOnMenuItemClickListener(null)
    }

    private fun showAddSongDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_song, null)

        val titleInput = dialogView.findViewById<android.widget.EditText>(R.id.inputTitle)
        val artistInput = dialogView.findViewById<android.widget.EditText>(R.id.inputArtist)
        val categoryInput = dialogView.findViewById<android.widget.EditText>(R.id.inputCategory)
        val lyricsInput = dialogView.findViewById<android.widget.EditText>(R.id.inputLyrics)
        val youtubeLinkInput =
            dialogView.findViewById<android.widget.EditText>(R.id.inputYoutubeLink)

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Add Song")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val title = titleInput.text.toString().trim()
                val artist = artistInput.text.toString().trim()
                val category = categoryInput.text.toString().trim()
                val lyrics = lyricsInput.text.toString().trim()
                val youtubeLink = youtubeLinkInput.text.toString().trim().nullIfBlank()

                if (title.isBlank() || lyrics.isBlank()) {
                    android.widget.Toast.makeText(
                        this,
                        "Song title and lyrics are required!",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                } else {
                    val song = com.guruguhan.lyricsapp.data.Song(
                        title = title,
                        artist = artist,
                        category = category,
                        lyrics = lyrics,
                        youtubeLink = youtubeLink
                    )
                    viewModel.insert(song)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showEditSongDialog(song: com.guruguhan.lyricsapp.data.Song) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_song, null)

        val titleInput = dialogView.findViewById<android.widget.EditText>(R.id.inputTitle)
        val artistInput = dialogView.findViewById<android.widget.EditText>(R.id.inputArtist)
        val categoryInput = dialogView.findViewById<android.widget.EditText>(R.id.inputCategory)
        val lyricsInput = dialogView.findViewById<android.widget.EditText>(R.id.inputLyrics)
        val youtubeLinkInput =
            dialogView.findViewById<android.widget.EditText>(R.id.inputYoutubeLink)

        // Pre-fill with existing song data
        titleInput.setText(song.title)
        artistInput.setText(song.artist)
        categoryInput.setText(song.category)
        lyricsInput.setText(song.lyrics)
        youtubeLinkInput.setText(song.youtubeLink)

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Edit Song")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val updatedTitle = titleInput.text.toString().trim()
                val updatedArtist = artistInput.text.toString().trim()
                val updatedCategory = categoryInput.text.toString().trim()
                val updatedLyrics = lyricsInput.text.toString().trim()
                val updatedYoutubeLink = youtubeLinkInput.text.toString().trim().nullIfBlank()

                if (updatedTitle.isBlank() || updatedLyrics.isBlank()) {
                    android.widget.Toast.makeText(
                        this,
                        "Song title and lyrics are required!",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                } else {
                    val updatedSong = song.copy(
                        title = updatedTitle,
                        artist = updatedArtist,
                        category = updatedCategory,
                        lyrics = updatedLyrics,
                        youtubeLink = updatedYoutubeLink
                    )
                    viewModel.update(updatedSong)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showDeleteConfirmationDialog(song: com.guruguhan.lyricsapp.data.Song) {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Delete Song")
            .setMessage("Are you sure you want to delete '${song.title}'?")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.delete(song)
                android.widget.Toast.makeText(
                    this,
                    "'${song.title}' deleted",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
                endManualActionMode() // Exit manual action mode
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    fun String.nullIfBlank(): String? {
        return if (this.isBlank()) null else this
    }
}