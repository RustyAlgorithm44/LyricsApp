package com.guruguhan.lyricsapp

import android.app.Application
import com.google.android.material.color.DynamicColors
import com.guruguhan.lyricsapp.ui.ThemeHelper

class LyricsApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // Apply the saved theme
        val theme = ThemeHelper.getTheme(this)
        ThemeHelper.applyTheme(theme)

        DynamicColors.applyToActivitiesIfAvailable(this)
    }
}
