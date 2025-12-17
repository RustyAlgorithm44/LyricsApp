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
            android.widget.Toast.makeText(this, "Hamburger menu clicked!", android.widget.Toast.LENGTH_SHORT).show()
            // Here you would typically open a navigation drawer
        }

        adapter = SongAdapter()

        val recyclerView =
            findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.songRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        // 🔑 Collect Flow properly
        lifecycleScope.launch {
            viewModel.allSongs.collect { songs ->
                adapter.submitList(songs)
            }
        }
        val fab = findViewById<com.google.android.material.floatingactionbutton.FloatingActionButton>(
            R.id.addSongFab
        )

        fab.setOnClickListener {
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
            fab.setOnLongClickListener {
                startActivity(Intent(this, SettingsActivity::class.java))
                true
            }
        }
        val searchInput = findViewById<android.widget.EditText>(R.id.searchInput)

        searchInput.addTextChangedListener(object : android.text.TextWatcher {
            override fun afterTextChanged(s: android.text.Editable?) {
                lifecycleScope.launch {
                    viewModel.search(s.toString()).collect { songs ->
                        adapter.submitList(songs)
                    }
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }
}

fun String.nullIfBlank(): String? {
    return if (this.isBlank()) null else this
}