package com.guruguhan.lyricsapp

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.guruguhan.lyricsapp.ui.SongAdapter
import com.guruguhan.lyricsapp.viewmodel.SongViewModel
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var adapter: SongAdapter
    private val viewModel: SongViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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

            androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Add Song")
                .setView(dialogView)
                .setPositiveButton("Save") { _, _ ->
                    val song = com.guruguhan.lyricsapp.data.Song(
                        title = titleInput.text.toString(),
                        artist = artistInput.text.toString(),
                        category = categoryInput.text.toString(),
                        lyrics = lyricsInput.text.toString()
                    )
                    viewModel.insert(song)
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }
}