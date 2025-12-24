package com.guruguhan.lyricsapp

import android.content.Intent
import android.os.Bundle
import android.view.ScaleGestureDetector
import android.view.MotionEvent
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import android.widget.Toast
import com.google.android.material.button.MaterialButton
import com.guruguhan.lyricsapp.data.Song
import com.guruguhan.lyricsapp.viewmodel.SongViewModel
import android.util.TypedValue
import android.graphics.Color
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SongDetailActivity : AppCompatActivity() {

    private lateinit var scaleGestureDetector: ScaleGestureDetector
    private var currentScaleFactor = 1.0f
    private var originalTextSize: Float = 0f
    private lateinit var detailLyricsTextView: TextView

    private val viewModel: SongViewModel by viewModels()
    private var song: Song? = null

    private lateinit var shareButton: MaterialButton
    private lateinit var favoriteButton: MaterialButton
    private lateinit var switchLanguageButton: MaterialButton

    private var languages: List<String> = emptyList()
    private var currentLanguageIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_song_detail)

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val typedValue = TypedValue()
        theme.resolveAttribute(com.google.android.material.R.attr.colorOnPrimary, typedValue, true)
        toolbar.navigationIcon?.setTint(typedValue.data)

        detailLyricsTextView = findViewById(R.id.detailLyrics)
        originalTextSize = detailLyricsTextView.textSize / resources.displayMetrics.scaledDensity
        scaleGestureDetector = ScaleGestureDetector(this, ScaleListener())

        val songId = intent.getIntExtra("SONG_ID", -1)
        if (songId == -1) {
            finish()
            return
        }

        shareButton = findViewById(R.id.shareButton)
        favoriteButton = findViewById(R.id.favoriteButton)
        switchLanguageButton = findViewById(R.id.switchLanguageButton)

        lifecycleScope.launch {
            viewModel.getSongById(songId).collectLatest { currentSong ->
                if (currentSong != null) {
                    song = currentSong
                    languages = currentSong.lyrics.keys.toList().sorted()
                    currentLanguageIndex = 0 // Reset index when song changes
                    updateUi(currentSong)
                }
            }
        }

        shareButton.setOnClickListener {
            song?.let {
                val shareText = "${it.title}\n${it.deity ?: ""}\n${it.composer}\n\n${detailLyricsTextView.text}"
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, shareText)
                }
                startActivity(Intent.createChooser(shareIntent, "Share lyrics via"))
            }
        }

        favoriteButton.setOnClickListener {
            song?.let { viewModel.toggleFavoriteStatus(it) }
        }

        switchLanguageButton.setOnClickListener {
            if (languages.size <= 1) {
                Toast.makeText(this, "No other language available.", Toast.LENGTH_SHORT).show()
            } else {
                currentLanguageIndex = (currentLanguageIndex + 1) % languages.size
                song?.let { updateLyricsDisplay(it) }
            }
        }
    }

    private fun updateUi(currentSong: Song) {
        findViewById<TextView>(R.id.detailTitle).text = currentSong.title
        findViewById<TextView>(R.id.detailComposer).text = currentSong.composer
        findViewById<TextView>(R.id.detailDeity).text = currentSong.deity ?: ""
        updateLyricsDisplay(currentSong) // Call to update lyrics based on currentLanguageIndex

        if (currentSong.isFavorite) {
            favoriteButton.setText("Favorited")
            favoriteButton.setIconResource(R.drawable.ic_star)
        } else {
            favoriteButton.setText("Favorite")
            favoriteButton.setIconResource(R.drawable.ic_star_border)
        }

        // Hide/Show language button based on available languages
        switchLanguageButton.visibility = if (languages.size > 1) android.view.View.VISIBLE else android.view.View.GONE
    }

    private fun updateLyricsDisplay(currentSong: Song) {
        val currentLanguage = languages.getOrNull(currentLanguageIndex)
        detailLyricsTextView.text = currentSong.lyrics[currentLanguage] ?: ""
    }

    override fun dispatchTouchEvent(event: MotionEvent?): Boolean {
        if (event != null) {
            scaleGestureDetector.onTouchEvent(event)
            if (scaleGestureDetector.isInProgress) {
                return true
            }
        }
        return super.dispatchTouchEvent(event)
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private inner class ScaleListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            currentScaleFactor *= detector.scaleFactor
            currentScaleFactor = Math.max(0.5f, Math.min(currentScaleFactor, 3.0f))
            detailLyricsTextView.textSize = originalTextSize * currentScaleFactor
            return true
        }
    }
}