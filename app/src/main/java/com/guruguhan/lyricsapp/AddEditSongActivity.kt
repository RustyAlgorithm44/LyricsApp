package com.guruguhan.lyricsapp

import android.app.Activity
import android.os.Bundle
import android.util.TypedValue
import android.view.inputmethod.EditorInfo
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import com.google.android.material.chip.Chip
import com.google.android.material.textfield.TextInputLayout
import com.guruguhan.lyricsapp.data.Song
import com.guruguhan.lyricsapp.databinding.ActivityAddEditSongBinding
import com.guruguhan.lyricsapp.ui.ItemMoveCallback
import com.guruguhan.lyricsapp.ui.LanguageLyricsAdapter
import com.guruguhan.lyricsapp.ui.LyricsData
import com.guruguhan.lyricsapp.viewmodel.SongViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.*

class AddEditSongActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddEditSongBinding
    private val songViewModel: SongViewModel by viewModels()
    private var songId: Int = -1
    private var currentSong: Song? = null
    private val allLanguages = listOf("English", "தமிழ்", "संस्कृतम्", "ಕನ್ನಡ", "Other")
    private lateinit var lyricsAdapter: LanguageLyricsAdapter
    private lateinit var itemTouchHelper: ItemTouchHelper


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddEditSongBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val typedValue = TypedValue()
        theme.resolveAttribute(com.google.android.material.R.attr.colorOnPrimary, typedValue, true)
        binding.toolbar.navigationIcon?.setTint(typedValue.data)

        setupRecyclerView()

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
            lyricsAdapter.items.add(LyricsData("", "", ""))
            lyricsAdapter.notifyItemInserted(0)
        }

        setupDeityInput()
        setupComposerInput()
        setupCategoryInput()

        binding.addLanguageButton.setOnClickListener {
            val usedLanguages = lyricsAdapter.getUsedLanguages()
            if (allLanguages.filterNot { it == "Other" }.all { usedLanguages.contains(it) }) {
                Toast.makeText(this, "All default languages have been added", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            lyricsAdapter.items.add(LyricsData("", "", ""))
            lyricsAdapter.notifyItemInserted(lyricsAdapter.itemCount - 1)
        }

        binding.saveButton.setOnClickListener {
            saveSong()
        }
    }

    private fun setupRecyclerView() {
        val adapterItems = mutableListOf<LyricsData>()
        lyricsAdapter = LanguageLyricsAdapter(this, adapterItems, allLanguages) { viewHolder ->
            itemTouchHelper.startDrag(viewHolder)
        }
        binding.lyricsContainer.adapter = lyricsAdapter

        val callback = ItemMoveCallback(lyricsAdapter)
        itemTouchHelper = ItemTouchHelper(callback)
        itemTouchHelper.attachToRecyclerView(binding.lyricsContainer)
    }

    private fun populateFields(song: Song) {
        binding.inputTitle.setText(song.title)
        binding.autoCompleteComposerInput.setText(song.composer)
        binding.inputRagam.setText(song.ragam)
        binding.autoCompleteDeityInput.setText(song.deity)

        binding.categoryChipGroup.removeAllViews()
        song.categories.forEach { category ->
            addCategoryChip(category)
        }

        binding.inputYoutubeLink.setText(song.youtubeLink)

        val lyricsList = song.lyrics.map { (lang, lyr) ->
            val markdownLyr = convertHtmlToMarkdown(lyr)
            val isOther = !allLanguages.contains(lang)
            LyricsData(
                language = if (isOther) "Other" else lang,
                lyrics = markdownLyr,
                otherLanguage = if (isOther) lang else ""
            )
        }.toMutableList()

        if (lyricsList.isNotEmpty()) {
            lyricsAdapter.items.addAll(lyricsList)
            lyricsAdapter.notifyDataSetChanged()
        } else {
            lyricsAdapter.items.add(LyricsData("", "", ""))
            lyricsAdapter.notifyItemInserted(0)
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

    private fun setupCategoryInput() {
        lifecycleScope.launch {
            val categories = songViewModel.uniqueCategories.first()
            if (categories.isNotEmpty()) {
                val adapter = ArrayAdapter(this@AddEditSongActivity, android.R.layout.simple_dropdown_item_1line, categories)
                binding.autoCompleteCategoryInput.setAdapter(adapter)
                binding.inputCategoryLayout.endIconMode = TextInputLayout.END_ICON_DROPDOWN_MENU
            } else {
                binding.inputCategoryLayout.endIconMode = TextInputLayout.END_ICON_NONE
            }
        }

        binding.autoCompleteCategoryInput.setOnItemClickListener { parent, _, position, _ ->
            val selectedCategory = parent.getItemAtPosition(position) as String
            addCategoryChip(selectedCategory)
            binding.autoCompleteCategoryInput.setText("") // Clear input
        }

        binding.autoCompleteCategoryInput.setOnEditorActionListener { v, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                val newCategory = v.text.toString().trim()
                if (newCategory.isNotEmpty()) {
                    addCategoryChip(newCategory)
                    v.text = "" // Clear input
                    return@setOnEditorActionListener true
                }
            }
            false
        }
    }

    private fun addCategoryChip(category: String) {
        for (i in 0 until binding.categoryChipGroup.childCount) {
            val chip = binding.categoryChipGroup.getChildAt(i) as Chip
            if (chip.text.toString().equals(category, ignoreCase = true)) {
                return
            }
        }

        val chip = Chip(this)
        chip.text = category
        chip.isCloseIconVisible = true
        chip.setOnCloseIconClickListener {
            binding.categoryChipGroup.removeView(it)
        }
        binding.categoryChipGroup.addView(chip)
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
        val categories = (0 until binding.categoryChipGroup.childCount).map {
            (binding.categoryChipGroup.getChildAt(it) as Chip).text.toString()
        }.toMutableList()

        val pendingCategory = binding.autoCompleteCategoryInput.text.toString().trim()
        if (pendingCategory.isNotEmpty() && !categories.any { it.equals(pendingCategory, ignoreCase = true) }) {
            categories.add(pendingCategory)
        }

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

        val formattedLyricsMap = LinkedHashMap<String, String>()
        val usedLanguages = mutableSetOf<String>()

        for (item in lyricsAdapter.items) {
            val lang = (if (item.language == "Other") item.otherLanguage else item.language).trim()
            val lyr = item.lyrics.trim()

            if (lang.isBlank() && lyr.isBlank()) {
                continue // Skip empty rows silently
            }

            if (lang.isBlank() || lyr.isBlank()) {
                Toast.makeText(this, "Found an incomplete language/lyrics pair.", Toast.LENGTH_SHORT).show()
                return
            }

            if (!usedLanguages.add(lang.lowercase(Locale.ROOT))) {
                Toast.makeText(this, "Duplicate language '$lang' found. Please remove it.", Toast.LENGTH_LONG).show()
                return
            }

            formattedLyricsMap[lang] = convertMarkdownToHtml(lyr)
        }


        if (formattedLyricsMap.isEmpty()) {
            Toast.makeText(this, "At least one complete language/lyrics pair is required", Toast.LENGTH_SHORT).show()
            return
        }

        val songToSave = currentSong?.copy(
            title = title,
            composer = composer,
            ragam = ragam,
            deity = deity,
            lyrics = formattedLyricsMap,
            categories = categories,
            youtubeLink = youtubeLink
        ) ?: Song(
            title = title,
            composer = composer,
            ragam = ragam,
            deity = deity,
            lyrics = formattedLyricsMap,
            categories = categories,
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

    private fun convertMarkdownToHtml(markdown: String): String {
        var html = markdown.replace("\n", "<br>")
        html = html.replace(Regex("(?s)\\*_((?:.|\\n)+?)\\_\\*"), "<b><u>$1</u></b>")
        html = html.replace(Regex("(?s)_\\*((?:.|\\n)+?)\\*_"), "<b><u>$1</u></b>")
        html = html.replace(Regex("(?s)\\*((?:.|\\n)+?)\\*"), "<b>$1</b>")
        html = html.replace(Regex("(?s)_((?:.|\\n)+?)_"), "<u>$1</u>")
        return html
    }

    private fun convertHtmlToMarkdown(html: String): String {
        var markdown = html
        markdown = markdown.replace(Regex("(?i)<b><u>((?:.|\\n)+?)</u></b>"), "*_$1_*")
        markdown = markdown.replace(Regex("(?i)<u><b>((?:.|\\n)+?)</b></u>"), "*_$1_*")
        markdown = markdown.replace(Regex("(?i)<b>((?:.|\\n)+?)</b>"), "*$1*")
        markdown = markdown.replace(Regex("(?i)<u>((?:.|\\n)+?)</u>"), "_$1_")
        markdown = markdown.replace(Regex("(?i)<br/?>"), "\n")
        return markdown
    }

    private fun String.nullIfBlank(): String? {
        return if (this.isBlank()) null else this
    }
}

