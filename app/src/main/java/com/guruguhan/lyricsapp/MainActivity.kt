package com.guruguhan.lyricsapp

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.guruguhan.lyricsapp.ui.SongAdapter
import com.guruguhan.lyricsapp.viewmodel.SongViewModel
import kotlinx.coroutines.launch
import android.content.Intent

class MainActivity : AppCompatActivity() {

    private lateinit var adapter: SongAdapter
    private val viewModel: SongViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = getString(R.string.app_name)

        toolbar.setNavigationOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }

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
                    findViewById<android.widget.TextView>(R.id.emptyStateTextView).visibility = android.view.View.VISIBLE
                } else {
                    findViewById<android.widget.TextView>(R.id.emptyStateTextView).visibility = android.view.View.GONE
                }
            }
        }

        val fab = findViewById<com.google.android.material.floatingactionbutton.FloatingActionButton>(
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
                            findViewById<android.widget.TextView>(R.id.noSearchResultsTextView).visibility = android.view.View.VISIBLE
                        } else {
                            findViewById<android.widget.TextView>(R.id.noSearchResultsTextView).visibility = android.view.View.GONE
                        }
                    }
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private var selectedSong: com.guruguhan.lyricsapp.data.Song? = null
    private var isInActionMode = false

    private fun startManualActionMode(song: com.guruguhan.lyricsapp.data.Song) {
        isInActionMode = true
        selectedSong = song
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        toolbar.menu.clear()
        toolbar.inflateMenu(R.menu.menu_contextual_action_mode)
        toolbar.setNavigationIcon(android.R.drawable.ic_menu_close_clear_cancel)
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
        toolbar.setNavigationIcon(R.drawable.ic_menu) // assuming you have ic_menu
        toolbar.setNavigationOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }
        toolbar.setOnMenuItemClickListener(null) // remove the contextual listener
    }

    private fun showAddSongDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_song, null)

        val titleInput = dialogView.findViewById<android.widget.EditText>(R.id.inputTitle)
        val artistInput = dialogView.findViewById<android.widget.EditText>(R.id.inputArtist)
        val categoryInput = dialogView.findViewById<android.widget.EditText>(R.id.inputCategory)
        val lyricsInput = dialogView.findViewById<android.widget.EditText>(R.id.inputLyrics)
        val youtubeLinkInput = dialogView.findViewById<android.widget.EditText>(R.id.inputYoutubeLink)

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
        val youtubeLinkInput = dialogView.findViewById<android.widget.EditText>(R.id.inputYoutubeLink)

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
                android.widget.Toast.makeText(this, "'${song.title}' deleted", android.widget.Toast.LENGTH_SHORT).show()
                endManualActionMode() // Exit manual action mode
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    fun String.nullIfBlank(): String? {
        return if (this.isBlank()) null else this
    }
}