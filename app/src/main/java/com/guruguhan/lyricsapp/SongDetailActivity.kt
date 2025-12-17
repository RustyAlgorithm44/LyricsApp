package com.guruguhan.lyricsapp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class SongDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_song_detail)

        val title = intent.getStringExtra("title") ?: ""
        val artist = intent.getStringExtra("artist") ?: ""
        val category = intent.getStringExtra("category") ?: ""
        val lyrics = intent.getStringExtra("lyrics") ?: ""

        findViewById<android.widget.TextView>(R.id.detailTitle).text = title
        findViewById<android.widget.TextView>(R.id.detailArtist).text = artist
        findViewById<android.widget.TextView>(R.id.detailCategory).text = category
        findViewById<android.widget.TextView>(R.id.detailLyrics).text = lyrics

        findViewById<android.widget.Button>(R.id.shareButton).setOnClickListener {
            val shareText = "$title – $artist\n\n$lyrics"
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, shareText)
            }
            startActivity(Intent.createChooser(shareIntent, "Share lyrics via"))
        }
    }
}