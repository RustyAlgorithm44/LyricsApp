package com.guruguhan.lyricsapp

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
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
    private var currentFilterType: String? = null // "DEITY" or "COMPOSER"
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
                // Already at home, just close the drawer
                drawerLayout.closeDrawer(GravityCompat.START)
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
        Toast.makeText(this, "Link to download the app will be available soon!", Toast.LENGTH_LONG).show()
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
            currentViewMode = ViewMode.ALL_SONGS
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

    private fun showAddSongDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_song, null)

        val titleInput = dialogView.findViewById<android.widget.EditText>(R.id.inputTitle)
        val composerInput = dialogView.findViewById<android.widget.EditText>(R.id.inputComposer)
        val deityInput = dialogView.findViewById<android.widget.EditText>(R.id.inputDeity)
        val lyricsInput = dialogView.findViewById<android.widget.EditText>(R.id.inputLyrics)
        val youtubeLinkInput =
            dialogView.findViewById<android.widget.EditText>(R.id.inputYoutubeLink)

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Add Song")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val title = titleInput.text.toString().trim()
                val composer = composerInput.text.toString().trim()
                val deity = deityInput.text.toString().trim().nullIfBlank()
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
                        composer = composer,
                        deity = deity,
                        lyrics = lyrics,
                        youtubeLink = youtubeLink
                    )
                    viewModel.insert(song)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showEditSongDialog(song: com.guruguhan.lyricsapp.data.Song) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_song, null)

        val titleInput = dialogView.findViewById<android.widget.EditText>(R.id.inputTitle)
        val composerInput = dialogView.findViewById<android.widget.EditText>(R.id.inputComposer)
        val deityInput = dialogView.findViewById<android.widget.EditText>(R.id.inputDeity)
        val lyricsInput = dialogView.findViewById<android.widget.EditText>(R.id.inputLyrics)
        val youtubeLinkInput =
            dialogView.findViewById<android.widget.EditText>(R.id.inputYoutubeLink)

        titleInput.setText(song.title)
        composerInput.setText(song.composer)
        deityInput.setText(song.deity)
        lyricsInput.setText(song.lyrics)
        youtubeLinkInput.setText(song.youtubeLink)

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Edit Song")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val updatedTitle = titleInput.text.toString().trim()
                val updatedComposer = composerInput.text.toString().trim()
                val updatedDeity = deityInput.text.toString().trim().nullIfBlank()
                val updatedLyrics = lyricsInput.text.toString().trim()
                val updatedYoutubeLink = youtubeLinkInput.text.toString().trim().nullIfBlank()

                if (updatedTitle.isBlank() || updatedLyrics.isBlank()) {
                    android.widget.Toast.makeText(
                        this,
                        "Song title and lyrics are required!",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                } else {
                    val updatedSong = song.copy(
                        title = updatedTitle,
                        composer = updatedComposer,
                        deity = updatedDeity,
                        lyrics = updatedLyrics,
                        youtubeLink = updatedYoutubeLink
                    )
                    viewModel.update(updatedSong)
                    endManualActionMode()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
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