package com.guruguhan.lyricsapp

import android.content.Intent
import android.os.Bundle
import android.view.ScaleGestureDetector
import android.view.MotionEvent
import androidx.appcompat.app.AppCompatActivity

class SongDetailActivity : AppCompatActivity() {

    private lateinit var scaleGestureDetector: ScaleGestureDetector
    private var currentScaleFactor = 1.0f
    private var originalTextSize: Float = 0f
    private lateinit var detailLyricsTextView: android.widget.TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_song_detail)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Lyrics"

        val title = intent.getStringExtra("title") ?: ""
        val composer = intent.getStringExtra("composer") ?: ""
        val deity = intent.getStringExtra("deity") ?: ""
        val lyrics = intent.getStringExtra("lyrics") ?: ""

        findViewById<android.widget.TextView>(R.id.detailTitle).text = title
        findViewById<android.widget.TextView>(R.id.detailComposer).text = composer
        findViewById<android.widget.TextView>(R.id.detailDeity).text = deity

        detailLyricsTextView = findViewById(R.id.detailLyrics)
        detailLyricsTextView.text = lyrics
        originalTextSize = detailLyricsTextView.textSize / resources.displayMetrics.scaledDensity

        scaleGestureDetector = ScaleGestureDetector(this, ScaleListener())

        findViewById<android.widget.Button>(R.id.shareButton).setOnClickListener {
            val shareText = "$title\n$deity\n$composer\n\n$lyrics"
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, shareText)
            }
            startActivity(Intent.createChooser(shareIntent, "Share lyrics via"))
        }
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

    override fun onTouchEvent(event: MotionEvent): Boolean {
        // This method is less critical now that dispatchTouchEvent is overridden,
        // but keeping it for completeness or if other touch handling is needed.
        return super.onTouchEvent(event)
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private inner class ScaleListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            currentScaleFactor *= detector.scaleFactor
            // Limit the zoom in/out
            currentScaleFactor = Math.max(0.5f, Math.min(currentScaleFactor, 3.0f))
            detailLyricsTextView.textSize = originalTextSize * currentScaleFactor
            return true
        }
    }
}