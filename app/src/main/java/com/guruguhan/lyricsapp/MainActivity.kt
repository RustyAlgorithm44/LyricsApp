package com.guruguhan.lyricsapp

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import android.widget.ArrayAdapter
import android.widget.EditText
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.chip.ChipGroup
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.guruguhan.lyricsapp.data.Song
import com.guruguhan.lyricsapp.ui.GroupAdapter
import com.guruguhan.lyricsapp.ui.SongAdapter
import android.view.MotionEvent
import com.guruguhan.lyricsapp.viewmodel.SongViewModel
import android.util.TypedValue
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var adapter: SongAdapter
    private lateinit var groupAdapter: GroupAdapter
    private lateinit var recyclerView: RecyclerView
    private val viewModel: SongViewModel by viewModels()

    private val actionModeTouchListener = object : RecyclerView.OnItemTouchListener {
        override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
            if (adapter.isInActionMode && rv.findChildViewUnder(e.x, e.y) == null) {
                endManualActionMode()
                return true
            }
            return false
        }

        override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {}
        override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {}
    }

    private enum class ViewMode {
        ALL_SONGS,
        DEITY_LIST,
        COMPOSER_LIST,
        SONGS_FILTERED
    }

    private var currentViewMode = ViewMode.ALL_SONGS
    private var observeJob: Job? = null
    private var currentFilterType: String? = null // "DEITY", "COMPOSER"
    private var currentFilterValue: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        drawerLayout = findViewById(R.id.drawer_layout)
        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener(this)

        toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        val typedValue = TypedValue()
        theme.resolveAttribute(com.google.android.material.R.attr.colorOnPrimary, typedValue, true)
        toggle.drawerArrowDrawable.color = typedValue.data

        adapter = SongAdapter(
            onItemClick = { song ->
                if (adapter.isInActionMode) {
                    toggleSelection(song)
                } else {
                    val intent = Intent(this, SongDetailActivity::class.java).apply {
                        putExtra("SONG_ID", song.id)
                    }
                    startActivity(intent)
                }
            },
            onItemLongClick = { song ->
                if (!adapter.isInActionMode) {
                    startManualActionMode(song)
                } else {
                    toggleSelection(song)
                }
            }
        )

        groupAdapter = GroupAdapter { itemName ->
            currentViewMode = ViewMode.SONGS_FILTERED
            currentFilterType = when (findViewById<ChipGroup>(R.id.chipGroup).checkedChipId) {
                R.id.chipDeity -> "DEITY"
                R.id.chipComposer -> "COMPOSER"
                else -> ""
            }
            currentFilterValue = itemName
            updateUI()
        }

        recyclerView = findViewById(R.id.songRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val chipGroup = findViewById<ChipGroup>(R.id.chipGroup)
        chipGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.chipAll -> {
                    currentViewMode = ViewMode.ALL_SONGS
                    updateUI()
                }
                R.id.chipDeity -> {
                    currentViewMode = ViewMode.DEITY_LIST
                    updateUI()
                }
                R.id.chipComposer -> {
                    currentViewMode = ViewMode.COMPOSER_LIST
                    updateUI()
                }
            }
        }

        updateUI() // Initial UI state

        lifecycleScope.launch {
            viewModel.errorEvents.collect { message ->
                Toast.makeText(this@MainActivity, message, Toast.LENGTH_LONG).show()
            }
        }

        val fab = findViewById<FloatingActionButton>(R.id.addSongFab)
        fab.setOnClickListener {
            showAddSongDialog()
        }

        val searchInput = findViewById<EditText>(R.id.searchInput)
        searchInput.addTextChangedListener(object : android.text.TextWatcher {
            override fun afterTextChanged(s: android.text.Editable?) {
                if (!s.isNullOrBlank()) {
                    recyclerView.adapter = adapter
                    observeJob?.cancel()
                    observeJob = lifecycleScope.launch {
                        viewModel.search(s.toString()).collect { songs ->
                            adapter.submitList(songs)
                            updateEmptyState(songs.isEmpty(), isSearch = true)
                        }
                    }
                } else {
                    updateUI()
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun updateUI() {
        val recyclerView = findViewById<RecyclerView>(R.id.songRecyclerView)
        observeJob?.cancel()

        when (currentViewMode) {
            ViewMode.ALL_SONGS -> {
                recyclerView.adapter = adapter
                observeJob = lifecycleScope.launch {
                    viewModel.allSongs.collect { songs ->
                        adapter.submitList(songs)
                        updateEmptyState(songs.isEmpty())
                    }
                }
            }
            ViewMode.DEITY_LIST -> {
                recyclerView.adapter = groupAdapter
                observeJob = lifecycleScope.launch {
                    viewModel.uniqueDeities.collect { deities ->
                        groupAdapter.submitList(deities)
                        updateEmptyState(deities.isEmpty())
                    }
                }
            }
            ViewMode.COMPOSER_LIST -> {
                recyclerView.adapter = groupAdapter
                observeJob = lifecycleScope.launch {
                    viewModel.uniqueComposers.collect { composers ->
                        groupAdapter.submitList(composers)
                        updateEmptyState(composers.isEmpty())
                    }
                }
            }
            ViewMode.SONGS_FILTERED -> {
                recyclerView.adapter = adapter
                observeJob = lifecycleScope.launch {
                    val flow = if (currentFilterType == "DEITY") {
                        viewModel.getSongsByDeity(currentFilterValue ?: "")
                    } else {
                        viewModel.getSongsByComposer(currentFilterValue ?: "")
                    }
                    flow.collect { songs ->
                        adapter.submitList(songs)
                        updateEmptyState(songs.isEmpty())
                    }
                }
            }
        }
    }

    private fun updateEmptyState(isEmpty: Boolean, isSearch: Boolean = false) {
        val emptyText = findViewById<android.widget.TextView>(R.id.emptyStateTextView)
        val noSearchText = findViewById<android.widget.TextView>(R.id.noSearchResultsTextView)

        if (isEmpty) {
            if (isSearch) {
                noSearchText.visibility = android.view.View.VISIBLE
                emptyText.visibility = android.view.View.GONE
            } else {
                emptyText.visibility = android.view.View.VISIBLE
                emptyText.text = if (currentViewMode == ViewMode.ALL_SONGS) "No songs yet. Tap '+' to add one!" else "No items found."
                noSearchText.visibility = android.view.View.GONE
            }
        } else {
            emptyText.visibility = android.view.View.GONE
            noSearchText.visibility = android.view.View.GONE
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_home -> {
                findViewById<Chip>(R.id.chipAll).isChecked = true
            }
            R.id.nav_favorites -> {
                val intent = Intent(this, FavoritesActivity::class.java)
                startActivity(intent)
            }
            R.id.nav_settings -> {
                val intent = Intent(this, SettingsActivity::class.java)
                startActivity(intent)
            }
            R.id.nav_share -> shareApk()
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun shareApk() {
        val shareText = "Check out the LyricsApp! Download it from: [Google Play Store link placeholder]"
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareText)
        }
        startActivity(Intent.createChooser(shareIntent, "Share App via"))
    }

    private fun toggleSelection(song: Song) {
        adapter.toggleSelection(song)
        val position = adapter.currentList.indexOf(song)
        if (position != -1) {
            adapter.notifyItemChanged(position)
        }

        val count = adapter.selectedSongs.size
        if (count == 0) {
            endManualActionMode()
        } else {
            val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
            toolbar.title = "$count selected"
            toolbar.menu.findItem(R.id.action_edit_song)?.isVisible = count == 1
        }
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else if (adapter.isInActionMode) {
            endManualActionMode()
        } else if (currentViewMode == ViewMode.SONGS_FILTERED) {
            if (currentFilterType == "DEITY") {
                currentViewMode = ViewMode.DEITY_LIST
            } else {
                currentViewMode = ViewMode.COMPOSER_LIST
            }
            updateUI()
        } else if (currentViewMode != ViewMode.ALL_SONGS) {
            findViewById<Chip>(R.id.chipAll).isChecked = true
        } else {
            super.onBackPressed()
        }
    }

    private fun startManualActionMode(song: Song) {
        adapter.isInActionMode = true
        recyclerView.addOnItemTouchListener(actionModeTouchListener)
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        toolbar.menu.clear()
        toolbar.inflateMenu(R.menu.menu_contextual_action_mode)
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
        toggle.isDrawerIndicatorEnabled = false
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationIcon(R.drawable.ic_close)
        toolbar.setNavigationOnClickListener { endManualActionMode() }

        toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.action_edit_song -> {
                    if (adapter.selectedSongs.size == 1) {
                        showEditSongDialog(adapter.selectedSongs.first())
                    }
                    true
                }
                R.id.action_delete_song -> {
                    showDeleteConfirmationDialog(adapter.selectedSongs.toList())
                    true
                }
                else -> false
            }
        }
        toggleSelection(song)
    }

    private fun endManualActionMode() {
        adapter.isInActionMode = false
        recyclerView.removeOnItemTouchListener(actionModeTouchListener)
        adapter.selectedSongs.clear()
        adapter.notifyDataSetChanged()

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        toolbar.title = getString(R.string.app_name)
        toolbar.menu.clear()
        toolbar.inflateMenu(R.menu.menu_main)
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
        toggle.isDrawerIndicatorEnabled = true
        supportActionBar?.setDisplayHomeAsUpEnabled(false)
        toggle.syncState()
        toolbar.setNavigationOnClickListener { drawerLayout.openDrawer(GravityCompat.START) }
    }

    private fun addLanguageInputRow(
        container: android.widget.LinearLayout,
        availableLanguages: List<String>,
        language: String = "",
        lyrics: String = ""
    ) {
        val inflater = layoutInflater
        val rowView = inflater.inflate(R.layout.item_language_input, container, false)

        val languageSpinner = rowView.findViewById<android.widget.Spinner>(R.id.languageSpinner)
        val otherLanguageLayout = rowView.findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.otherLanguageLayout)
        val otherLanguageInput = rowView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.otherLanguageInput)
        val lyricsInput = rowView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.inputLyrics)
        val removeButton = rowView.findViewById<android.widget.ImageButton>(R.id.removeLanguageButton)

        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, availableLanguages)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        languageSpinner.adapter = spinnerAdapter

        lyricsInput.setText(lyrics)

        // Set spinner selection based on existing language
        val languagePosition = availableLanguages.indexOf(language)
        if (language.isNotBlank() && languagePosition != -1) {
            languageSpinner.setSelection(languagePosition)
        } else if (language.isNotBlank()) {
            // If the language is custom, select "Other" and fill the field
            languageSpinner.setSelection(availableLanguages.indexOf("Other"))
            otherLanguageLayout.visibility = android.view.View.VISIBLE
            otherLanguageInput.setText(language)
        }


        languageSpinner.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                if (availableLanguages[position] == "Other") {
                    otherLanguageLayout.visibility = android.view.View.VISIBLE
                } else {
                    otherLanguageLayout.visibility = android.view.View.GONE
                }
            }
            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {
                otherLanguageLayout.visibility = android.view.View.GONE
            }
        }

        removeButton.setOnClickListener {
            container.removeView(rowView)
        }

        container.addView(rowView)
    }

    private fun showAddSongDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_song, null)

        val titleInput = dialogView.findViewById<android.widget.EditText>(R.id.inputTitle)
        val inputDeityPlain = dialogView.findViewById<android.widget.EditText>(R.id.inputDeityPlain)
        val inputDeityDropdownLayout = dialogView.findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.inputDeityDropdownLayout)
        val autoCompleteDeityInput = dialogView.findViewById<android.widget.AutoCompleteTextView>(R.id.autoCompleteDeityInput)
        val composerInput = dialogView.findViewById<android.widget.EditText>(R.id.inputComposer)
        val youtubeLinkInput =
            dialogView.findViewById<android.widget.EditText>(R.id.inputYoutubeLink)
        val lyricsContainer = dialogView.findViewById<android.widget.LinearLayout>(R.id.lyricsContainer)
        val addLanguageButton = dialogView.findViewById<com.google.android.material.button.MaterialButton>(R.id.addLanguageButton)
        val allLanguages = listOf("English", "தமிழ்", "संस्कृतम्", "ಕನ್ನಡ", "Other")

        // Add one empty row to start
        addLanguageInputRow(lyricsContainer, allLanguages)

        addLanguageButton.setOnClickListener {
            val usedLanguages = mutableSetOf<String>()
            for (i in 0 until lyricsContainer.childCount) {
                val rowView = lyricsContainer.getChildAt(i)
                val spinner = rowView.findViewById<android.widget.Spinner>(R.id.languageSpinner)
                if (spinner.selectedItem != null) {
                    usedLanguages.add(spinner.selectedItem.toString())
                }
            }
            val availableLanguages = allLanguages.filter { it !in usedLanguages || it == "Other" }
            if (availableLanguages.size == 1 && availableLanguages.contains("Other")) {
                Toast.makeText(this, "All languages have been added", Toast.LENGTH_SHORT).show()
            } else {
                addLanguageInputRow(lyricsContainer, availableLanguages)
            }
        }

        lifecycleScope.launch {
            viewModel.uniqueDeities.collect { deities ->
                if (deities.isEmpty()) {
                    inputDeityPlain.visibility = android.view.View.VISIBLE
                    inputDeityDropdownLayout.visibility = android.view.View.GONE
                } else {
                    inputDeityPlain.visibility = android.view.View.GONE
                    inputDeityDropdownLayout.visibility = android.view.View.VISIBLE
                    val adapter = ArrayAdapter(
                        this@MainActivity,
                        android.R.layout.simple_dropdown_item_1line,
                        deities
                    )
                    autoCompleteDeityInput.setAdapter(adapter)
                }
            }
        }

        val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Add Song")
            .setView(dialogView)
            .setPositiveButton("Save", null) // Set null listener initially
            .setNegativeButton("Cancel", null)
            .show()

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val title = titleInput.text.toString().trim()
            val composer = composerInput.text.toString().trim()
            val deity = if (inputDeityPlain.visibility == android.view.View.VISIBLE) {
                inputDeityPlain.text.toString().trim().nullIfBlank()
            } else {
                autoCompleteDeityInput.text.toString().trim().nullIfBlank()
            }
            val youtubeLink = youtubeLinkInput.text.toString().trim().nullIfBlank()

            titleInput.error = null // Clear previous errors

            val lyricsMap = mutableMapOf<String, String>()
            var isValidLyrics = true
            for (i in 0 until lyricsContainer.childCount) {
                val rowView = lyricsContainer.getChildAt(i)
                val languageSpinner = rowView.findViewById<android.widget.Spinner>(R.id.languageSpinner)
                val otherLanguageLayout = rowView.findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.otherLanguageLayout)
                val otherLanguageInput = rowView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.otherLanguageInput)
                val lyricsInput = rowView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.inputLyrics)

                val selectedLanguage = languageSpinner.selectedItem.toString()
                val lang = if (selectedLanguage == "Other") {
                    otherLanguageInput.text?.toString()?.trim() ?: ""
                } else {
                    selectedLanguage
                }
                val lyr = lyricsInput.text?.toString()?.trim() ?: ""

                // Clear previous errors for this row
                if (selectedLanguage == "Other") {
                    otherLanguageLayout.error = null
                }
                // Assuming lyricsInput is inside a TextInputLayout, if not, adjust
                (lyricsInput.parent.parent as? TextInputLayout)?.error = null


                if (lang.isNotBlank() && lyr.isNotBlank()) {
                    lyricsMap[lang] = lyr
                } else if (lang.isNotBlank() || lyr.isNotBlank()) {
                    // Only one field is filled, mark as invalid
                    isValidLyrics = false
                    if (selectedLanguage == "Other" && otherLanguageInput.text.isNullOrBlank()) {
                        otherLanguageLayout.error = "Language required"
                    }
                    if (lyricsInput.text.isNullOrBlank()) {
                        (lyricsInput.parent.parent as? TextInputLayout)?.error = "Lyrics required"
                    }
                }
            }

            if (title.isBlank()) {
                titleInput.error = "Song title is required!"
                return@setOnClickListener
            }

            if (deity.isNullOrBlank()) {
                if (inputDeityPlain.visibility == android.view.View.VISIBLE) {
                    inputDeityPlain.error = "Deity is required!"
                } else {
                    inputDeityDropdownLayout.error = "Deity is required!"
                }
                return@setOnClickListener
            }

            if (lyricsMap.isEmpty() && lyricsContainer.childCount == 0) { // No lyrics rows at all
                android.widget.Toast.makeText(
                    this,
                    "At least one complete language/lyrics pair is required!",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            } else if (!isValidLyrics) {
                android.widget.Toast.makeText(
                    this,
                    "Incomplete lyrics entry found. Please complete or clear the row.",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }


            val song = com.guruguhan.lyricsapp.data.Song(
                title = title,
                composer = composer,
                deity = deity,
                lyrics = lyricsMap,
                youtubeLink = youtubeLink
            )
            viewModel.insert(song)
            dialog.dismiss()
        }
    }

    private fun showEditSongDialog(song: com.guruguhan.lyricsapp.data.Song) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_song, null)

        val titleInput = dialogView.findViewById<android.widget.EditText>(R.id.inputTitle)
        val inputDeityPlain = dialogView.findViewById<android.widget.EditText>(R.id.inputDeityPlain)
        val inputDeityDropdownLayout = dialogView.findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.inputDeityDropdownLayout)
        val autoCompleteDeityInput = dialogView.findViewById<android.widget.AutoCompleteTextView>(R.id.autoCompleteDeityInput)
        val composerInput = dialogView.findViewById<android.widget.EditText>(R.id.inputComposer)
        val youtubeLinkInput =
            dialogView.findViewById<android.widget.EditText>(R.id.inputYoutubeLink)
        val lyricsContainer = dialogView.findViewById<android.widget.LinearLayout>(R.id.lyricsContainer)
        val addLanguageButton = dialogView.findViewById<com.google.android.material.button.MaterialButton>(R.id.addLanguageButton)
        val allLanguages = listOf("English", "தமிழ்", "संस्कृतम्", "ಕನ್ನಡ", "Other")

        titleInput.setText(song.title)
        composerInput.setText(song.composer)
        youtubeLinkInput.setText(song.youtubeLink)

        // Populate existing lyrics - for simplicity in edit mode, we provide all languages to each spinner.
        if (song.lyrics.isNotEmpty()) {
            song.lyrics.forEach { (lang, lyr) ->
                // Pass the full list, but also the specific language to be selected
                addLanguageInputRow(lyricsContainer, allLanguages, lang, lyr)
            }
        } else {
            addLanguageInputRow(lyricsContainer, allLanguages) // Add an empty row if no lyrics exist
        }

        addLanguageButton.setOnClickListener {
            val usedLanguages = mutableSetOf<String>()
            for (i in 0 until lyricsContainer.childCount) {
                val rowView = lyricsContainer.getChildAt(i)
                val spinner = rowView.findViewById<android.widget.Spinner>(R.id.languageSpinner)
                 if (spinner.selectedItem != null) {
                    usedLanguages.add(spinner.selectedItem.toString())
                }
            }
            val availableLanguages = allLanguages.filter { it !in usedLanguages || it == "Other" }
            if (availableLanguages.size == 1 && availableLanguages.contains("Other")) {
                Toast.makeText(this, "All languages have been added", Toast.LENGTH_SHORT).show()
            } else {
                addLanguageInputRow(lyricsContainer, availableLanguages)
            }
        }

        lifecycleScope.launch {
            viewModel.uniqueDeities.collect { deities ->
                if (deities.isEmpty()) {
                    inputDeityPlain.visibility = android.view.View.VISIBLE
                    inputDeityDropdownLayout.visibility = android.view.View.GONE
                    inputDeityPlain.setText(song.deity) // Set existing deity to plain EditText
                } else {
                    inputDeityPlain.visibility = android.view.View.GONE
                    inputDeityDropdownLayout.visibility = android.view.View.VISIBLE
                    val adapter = ArrayAdapter(
                        this@MainActivity,
                        android.R.layout.simple_dropdown_item_1line,
                        deities
                    )
                    autoCompleteDeityInput.setAdapter(adapter)
                    autoCompleteDeityInput.setText(song.deity) // Set existing deity to AutoCompleteTextView
                }
            }
        }

        val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Edit Song")
            .setView(dialogView)
            .setPositiveButton("Save", null) // Set null listener initially
            .setNegativeButton("Cancel", null)
            .show()

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val updatedTitle = titleInput.text.toString().trim()
            val updatedComposer = composerInput.text.toString().trim()
            val updatedDeity = if (inputDeityPlain.visibility == android.view.View.VISIBLE) {
                inputDeityPlain.text.toString().trim().nullIfBlank()
            } else {
                autoCompleteDeityInput.text.toString().trim().nullIfBlank()
            }
            val updatedYoutubeLink = youtubeLinkInput.text.toString().trim().nullIfBlank()

            titleInput.error = null // Clear previous errors

            val lyricsMap = mutableMapOf<String, String>()
            var isValidLyrics = true
            for (i in 0 until lyricsContainer.childCount) {
                val rowView = lyricsContainer.getChildAt(i)
                val languageSpinner = rowView.findViewById<android.widget.Spinner>(R.id.languageSpinner)
                val otherLanguageLayout = rowView.findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.otherLanguageLayout)
                val otherLanguageInput = rowView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.otherLanguageInput)
                val lyricsInput = rowView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.inputLyrics)

                val selectedLanguage = languageSpinner.selectedItem.toString()
                val lang = if (selectedLanguage == "Other") {
                    otherLanguageInput.text?.toString()?.trim() ?: ""
                } else {
                    selectedLanguage
                }
                val lyr = lyricsInput.text?.toString()?.trim() ?: ""

                // Clear previous errors for this row
                if (selectedLanguage == "Other") {
                    otherLanguageLayout.error = null
                }
                (lyricsInput.parent.parent as? TextInputLayout)?.error = null

                if (lang.isNotBlank() && lyr.isNotBlank()) {
                    lyricsMap[lang] = lyr
                } else if (lang.isNotBlank() || lyr.isNotBlank()) {
                    // Only one field is filled, mark as invalid
                    isValidLyrics = false
                    if (selectedLanguage == "Other" && otherLanguageInput.text.isNullOrBlank()) {
                        otherLanguageLayout.error = "Language required"
                    }
                    if (lyricsInput.text.isNullOrBlank()) {
                        (lyricsInput.parent.parent as? TextInputLayout)?.error = "Lyrics required"
                    }
                }
            }

            if (updatedTitle.isBlank()) {
                titleInput.error = "Song title is required!"
                return@setOnClickListener
            }

            if (updatedDeity.isNullOrBlank()) {
                if (inputDeityPlain.visibility == android.view.View.VISIBLE) {
                    inputDeityPlain.error = "Deity is required!"
                } else {
                    inputDeityDropdownLayout.error = "Deity is required!"
                }
                return@setOnClickListener
            }

            if (lyricsMap.isEmpty() && lyricsContainer.childCount == 0) { // No lyrics rows at all
                android.widget.Toast.makeText(
                    this,
                    "At least one complete language/lyrics pair are required!",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            } else if (!isValidLyrics) {
                android.widget.Toast.makeText(
                    this,
                    "Incomplete lyrics entry found. Please complete or clear the row.",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            val updatedSong = song.copy(
                title = updatedTitle,
                composer = updatedComposer,
                deity = updatedDeity,
                lyrics = lyricsMap,
                youtubeLink = updatedYoutubeLink
            )
            viewModel.update(updatedSong)
            dialog.dismiss()
            endManualActionMode()
        }
    }

    override fun onResume() {
        super.onResume()
        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        navigationView.menu.findItem(R.id.nav_home).isChecked = true
    }

    private fun showDeleteConfirmationDialog(songs: List<com.guruguhan.lyricsapp.data.Song>) {
        val message = if (songs.size == 1) {
            "Are you sure you want to delete '${songs.first().title}'?"
        } else {
            "Are you sure you want to delete ${songs.size} songs?"
        }
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Delete Song")
            .setMessage(message)
            .setPositiveButton("Delete") { _, _ ->
                songs.forEach { viewModel.delete(it) }
                val toastMessage = if (songs.size == 1) {
                    "'${songs.first().title}' deleted"
                } else {
                    "${songs.size} songs deleted"
                }
                android.widget.Toast.makeText(
                    this,
                    toastMessage,
                    android.widget.Toast.LENGTH_SHORT
                ).show()
                endManualActionMode()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    fun String.nullIfBlank(): String? {
        return if (this.isBlank()) null else this
    }
}
