package com.guruguhan.lyricsapp

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.TypedValue
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputLayout
import com.guruguhan.lyricsapp.R
import com.guruguhan.lyricsapp.MainActivity
import com.guruguhan.lyricsapp.FavoritesActivity
import com.guruguhan.lyricsapp.SettingsActivity
import com.guruguhan.lyricsapp.data.Song
import com.guruguhan.lyricsapp.databinding.ActivityAddEditSongBinding
import com.guruguhan.lyricsapp.databinding.ItemLanguageInputBinding
import com.guruguhan.lyricsapp.viewmodel.SongViewModel
import android.text.Html
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class AddEditSongActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddEditSongBinding
    private val songViewModel: SongViewModel by viewModels()
    private var songId: Int = -1
    private var currentSong: Song? = null
    private val allLanguages = listOf("English", "தமிழ்", "संस्कृतम्", "ಕನ್ನಡ", "Other")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddEditSongBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val typedValue = TypedValue()
        theme.resolveAttribute(com.google.android.material.R.attr.colorOnPrimary, typedValue, true)
        binding.toolbar.navigationIcon?.setTint(typedValue.data)

        songId = intent.getIntExtra("SONG_ID", -1)

        if (songId != -1) {
            supportActionBar?.title = "Edit Song"
            lifecycleScope.launch {
                currentSong = songViewModel.getSongById(songId).first()
                currentSong?.let {
                    populateFields(it)
                }
            }
        } else {
            supportActionBar?.title = "Add Song"
            addLanguageInputRow() // Add an initial empty row
        }

        setupDeityInput()
        setupComposerInput()

        binding.addLanguageButton.setOnClickListener {
            val usedLanguages = mutableSetOf<String>()
            for (i in 0 until binding.lyricsContainer.childCount) {
                val rowView = binding.lyricsContainer.getChildAt(i)
                val rowBinding = ItemLanguageInputBinding.bind(rowView)
                val selectedLanguage = rowBinding.languageSpinner.selectedItem.toString()

                if (selectedLanguage != "Other") {
                    usedLanguages.add(selectedLanguage)
                } else {
                    val otherLang = rowBinding.otherLanguageInput.text.toString().trim()
                    if (otherLang.isNotBlank()) usedLanguages.add(otherLang)
                }
            }
            val availableLanguages = allLanguages.filter { it !in usedLanguages || it == "Other" }
            if (availableLanguages.size == 1 && availableLanguages.contains("Other") && usedLanguages.contains("Other")) {
                Toast.makeText(this, "All languages have been added", Toast.LENGTH_SHORT).show()
            }
            else {
                addLanguageInputRow(availableLanguages = availableLanguages)
            }
        }

        binding.saveButton.setOnClickListener {
            saveSong()
        }
    }
    private fun populateFields(song: Song) {
        binding.inputTitle.setText(song.title)
        binding.autoCompleteComposerInput.setText(song.composer)
        binding.inputRagam.setText(song.ragam)
        binding.autoCompleteDeityInput.setText(song.deity)
        binding.inputYoutubeLink.setText(song.youtubeLink)
        if (song.lyrics.isNotEmpty()) {
            song.lyrics.forEach { (lang, lyr) ->
                addLanguageInputRow(language = lang, lyrics = lyr)
            }
        } else {
            addLanguageInputRow()
        }
    }

    private fun setupDeityInput() {
        lifecycleScope.launch {
            val deities = songViewModel.uniqueDeities.first()
            if (deities.isNotEmpty()) {
                val adapter = ArrayAdapter(this@AddEditSongActivity, android.R.layout.simple_dropdown_item_1line, deities)
                binding.autoCompleteDeityInput.setAdapter(adapter)
                binding.inputDeityDropdownLayout.endIconMode = TextInputLayout.END_ICON_DROPDOWN_MENU
            } else {
                binding.inputDeityDropdownLayout.endIconMode = TextInputLayout.END_ICON_NONE
            }
        }
    }

    private fun setupComposerInput() {
        lifecycleScope.launch {
            val composers = songViewModel.uniqueComposers.first()
            if (composers.isNotEmpty()) {
                val adapter = ArrayAdapter(this@AddEditSongActivity, android.R.layout.simple_dropdown_item_1line, composers)
                binding.autoCompleteComposerInput.setAdapter(adapter)
                binding.inputComposerDropdownLayout.endIconMode = TextInputLayout.END_ICON_DROPDOWN_MENU
            } else {
                binding.inputComposerDropdownLayout.endIconMode = TextInputLayout.END_ICON_NONE
            }
        }
    }

    private fun addLanguageInputRow(availableLanguages: List<String> = allLanguages, language: String = "", lyrics: String = "") {
        val rowBinding = ItemLanguageInputBinding.inflate(layoutInflater, binding.lyricsContainer, false)

        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, availableLanguages)
        rowBinding.languageSpinner.adapter = spinnerAdapter

        // Convert HTML to plain text for editing
        val plainTextLyrics = if (lyrics.isNotEmpty()) {
            Html.fromHtml(lyrics, Html.FROM_HTML_MODE_LEGACY).toString()
        } else {
            ""
        }
        rowBinding.inputLyrics.setText(plainTextLyrics)

        val languagePosition = availableLanguages.indexOf(language)
        if (language.isNotBlank() && languagePosition != -1) {
            rowBinding.languageSpinner.setSelection(languagePosition)
        } else if (language.isNotBlank()) {
            rowBinding.languageSpinner.setSelection(availableLanguages.indexOf("Other"))
            rowBinding.otherLanguageLayout.visibility = android.view.View.VISIBLE
            rowBinding.otherLanguageInput.setText(language)
        }


        rowBinding.languageSpinner.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                if (availableLanguages[position] == "Other") {
                    rowBinding.otherLanguageLayout.visibility = android.view.View.VISIBLE
                }
                else {
                    rowBinding.otherLanguageLayout.visibility = android.view.View.GONE
                }
            }
            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {
                rowBinding.otherLanguageLayout.visibility = android.view.View.GONE
            }
        }

        rowBinding.removeLanguageButton.setOnClickListener {
            binding.lyricsContainer.removeView(rowBinding.root)
        }

        binding.lyricsContainer.addView(rowBinding.root)
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun saveSong() {
        val title = binding.inputTitle.text.toString().trim()
        val composer = binding.autoCompleteComposerInput.text.toString().trim()
        val ragam = binding.inputRagam.text.toString().trim()
        val deity = binding.autoCompleteDeityInput.text.toString().trim()
        val youtubeLink = binding.inputYoutubeLink.text.toString().trim().nullIfBlank()

        binding.inputTitleLayout.error = null
        binding.inputDeityDropdownLayout.error = null

        if (title.isBlank()) {
            binding.inputTitleLayout.error = "Song title is required!"
            return
        }

        if (deity.isBlank()) {
            binding.inputDeityDropdownLayout.error = "Deity is required!"
            return
        }

        val lyricsMap = mutableMapOf<String, String>()
        var isValidLyrics = true
        for (i in 0 until binding.lyricsContainer.childCount) {
            val rowView = binding.lyricsContainer.getChildAt(i)
            val rowBinding = ItemLanguageInputBinding.bind(rowView)

            val selectedLanguage = rowBinding.languageSpinner.selectedItem.toString()
            val lang = if (selectedLanguage == "Other") {
                rowBinding.otherLanguageInput.text?.toString()?.trim() ?: ""
            } else {
                selectedLanguage
            }
            val lyr = rowBinding.inputLyrics.text?.toString()?.trim() ?: ""

            // Clear previous errors
            rowBinding.otherLanguageLayout.error = null
            rowBinding.inputLyricsLayout.error = null

            if (lang.isNotBlank() && lyr.isNotBlank()) {
                lyricsMap[lang] = lyr
            } else if (lang.isNotBlank() || lyr.isNotBlank() || (selectedLanguage == "Other" && (lang.isNotBlank() || lyr.isNotBlank())) ) {
                // This condition catches partially filled rows.
                if (lang.isBlank() && selectedLanguage == "Other") {
                    rowBinding.otherLanguageLayout.error = "Language required"
                    isValidLyrics = false
                }
                if (lyr.isBlank()) {
                    rowBinding.inputLyricsLayout.error = "Lyrics required"
                    isValidLyrics = false
                }
            }
        }

        if (!isValidLyrics) {
            return // Stop if any row is incomplete
        }

        if (lyricsMap.isEmpty()) {
            if (binding.lyricsContainer.childCount > 0) {
                val firstRow = binding.lyricsContainer.getChildAt(0)
                val firstRowBinding = ItemLanguageInputBinding.bind(firstRow)
                firstRowBinding.inputLyricsLayout.error = "At least one complete language/lyrics pair is required"
            } else {
                // This case should ideally not be hit if there's always at least one row, but as a fallback:
                Toast.makeText(this, "At least one complete language/lyrics pair is required", Toast.LENGTH_SHORT).show()
            }
            return
        }

        val formattedLyricsMap = mutableMapOf<String, String>()
        lyricsMap.forEach { (lang, lyr) ->
            var formattedLyr = lyr.replace("\n", "<br>")
            formattedLyr = formattedLyr.replace(Regex("pallavi\\s*:?", RegexOption.IGNORE_CASE), "<b><u>Pallavi:</u></b>")
            formattedLyr = formattedLyr.replace(Regex("anupallavi\\s*:?", RegexOption.IGNORE_CASE), "<b><u>Anupallavi:</u></b>")
            formattedLyr = formattedLyr.replace(Regex("ch?ara(n|N)am\\s*:?", RegexOption.IGNORE_CASE), "<b><u>Charanam:</u></b>")
            formattedLyricsMap[lang] = formattedLyr
        }

        val songToSave = currentSong?.copy(
            title = title,
            composer = composer,
            ragam = ragam,
            deity = deity,
            lyrics = formattedLyricsMap,
            youtubeLink = youtubeLink
        ) ?: Song(
            title = title,
            composer = composer,
            ragam = ragam,
            deity = deity,
            lyrics = formattedLyricsMap,
            youtubeLink = youtubeLink
        )

        if (songId == -1) {
            songViewModel.insert(songToSave)
            Toast.makeText(this, "Song added", Toast.LENGTH_SHORT).show()
        } else {
            songViewModel.update(songToSave)
            Toast.makeText(this, "Song updated", Toast.LENGTH_SHORT).show()
        }
        setResult(Activity.RESULT_OK)
        finish()
    }

    private fun String.nullIfBlank(): String? {
        return if (this.isBlank()) null else this
    }
}
