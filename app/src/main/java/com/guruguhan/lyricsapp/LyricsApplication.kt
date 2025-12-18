package com.guruguhan.lyricsapp

import android.app.Application
import com.google.android.material.color.DynamicColors

class LyricsApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        DynamicColors.applyToActivitiesIfAvailable(this)
    }
}
