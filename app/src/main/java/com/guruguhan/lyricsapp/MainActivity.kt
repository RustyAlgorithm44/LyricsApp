package com.guruguhan.lyricsapp

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.widget.doAfterTextChanged
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.guruguhan.lyricsapp.fragments.AllSongsFragment
import com.guruguhan.lyricsapp.viewmodel.SongViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var toolbar: androidx.appcompat.widget.Toolbar

    private lateinit var viewPager: ViewPager2
    private val viewModel: SongViewModel by viewModels()

    private var isInActionMode = false

    private val viewPagerPageChangeCallback = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            val chipGroup = findViewById<ChipGroup>(R.id.chipGroup)
            val chipId = when (position) {
                0 -> R.id.chipAll
                1 -> R.id.chipDeity
                2 -> R.id.chipComposer
                3 -> R.id.chipCategory
                else -> R.id.chipAll
            }
            if (chipGroup.checkedChipId != chipId) {
                chipGroup.check(chipId)
            }
            // Exit action mode when swiping to a different page
            if (isInActionMode) {
                viewModel.clearSelection()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        drawerLayout = findViewById(R.id.drawer_layout)
        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener(this)

        setupDrawer()
        setupViewPager()
        setupChipGroup()
        setupObservers()
        setupFab()
        setupSearch()
    }

    private fun setupDrawer() {
        toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        val typedValue = android.util.TypedValue()
        theme.resolveAttribute(com.google.android.material.R.attr.colorOnPrimary, typedValue, true)
        toggle.drawerArrowDrawable.color = typedValue.data
    }

    private fun setupViewPager() {
        viewPager = findViewById(R.id.viewPager)
        viewPager.adapter = ViewPagerAdapter(this)
        viewPager.registerOnPageChangeCallback(viewPagerPageChangeCallback)
    }

    private fun setupChipGroup() {
        val chipGroup = findViewById<ChipGroup>(R.id.chipGroup)
        chipGroup.setOnCheckedChangeListener { _, checkedId ->
            val position = when (checkedId) {
                R.id.chipAll -> 0
                R.id.chipDeity -> 1
                R.id.chipComposer -> 2
                R.id.chipCategory -> 3
                else -> 0
            }
            if (viewPager.currentItem != position) {
                viewPager.currentItem = position
            }
        }
        chipGroup.check(R.id.chipAll)
    }

    private fun setupFab() {
        val fab = findViewById<FloatingActionButton>(R.id.addSongFab)
        fab.setOnClickListener {
            val intent = Intent(this, AddEditSongActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupSearch() {
        val searchInput = findViewById<EditText>(R.id.searchInput)
        searchInput.doAfterTextChanged { text ->
            viewModel.setSearchQuery(text?.toString())
        }
        setupUI(drawerLayout)
    }

    private fun setupObservers() {
        lifecycleScope.launch {
            viewModel.errorEvents.collect { message ->
                Toast.makeText(this@MainActivity, message, Toast.LENGTH_LONG).show()
            }
        }

        lifecycleScope.launch {
            combine(viewModel.isInActionMode, viewModel.selectedSongs) { inActionMode, selectedSongs ->
                inActionMode to selectedSongs
            }.collectLatest { (inActionMode, selectedSongs) ->
                val changed = this@MainActivity.isInActionMode != inActionMode
                this@MainActivity.isInActionMode = inActionMode

                if (changed) {
                    invalidateOptionsMenu()
                } else if (inActionMode) {
                    // This is needed to update the 'edit' button visibility when selection changes from 1 to >1 or vice-versa
                    invalidateOptionsMenu()
                }
                updateToolbar()
            }
        }
    }

    private fun updateToolbar() {
        if (isInActionMode) {
            val selectedCount = viewModel.selectedSongs.value.size
            toolbar.title = "$selectedCount selected"

            val typedValue = android.util.TypedValue()
            theme.resolveAttribute(com.google.android.material.R.attr.colorOnPrimary, typedValue, true)
            val tintedDrawable = getDrawable(R.drawable.ic_close)?.mutate()
            tintedDrawable?.setTint(typedValue.data)

            toolbar.navigationIcon = tintedDrawable
            toolbar.setNavigationOnClickListener { viewModel.clearSelection() }
            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
            viewPager.isUserInputEnabled = false

            findViewById<androidx.coordinatorlayout.widget.CoordinatorLayout>(R.id.coordinator_layout).setOnClickListener {
                viewModel.clearSelection()
            }
            findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.searchInputLayout).setOnClickListener {
                viewModel.clearSelection()
            }
            findViewById<com.google.android.material.chip.ChipGroup>(R.id.chipGroup).setOnClickListener {
                viewModel.clearSelection()
            }
        } else {
            toolbar.title = getString(R.string.app_name)
            setupDrawer() // Resets toggle and listener
            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
            viewPager.isUserInputEnabled = true
            findViewById<androidx.coordinatorlayout.widget.CoordinatorLayout>(R.id.coordinator_layout).setOnClickListener(null)
            findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.searchInputLayout).setOnClickListener(null)
            findViewById<com.google.android.material.chip.ChipGroup>(R.id.chipGroup).setOnClickListener(null)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        if (isInActionMode) {
            menuInflater.inflate(R.menu.menu_contextual_action_mode, menu)
        }
        // No default menu to inflate otherwise
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        if (isInActionMode) {
            val selectedCount = viewModel.selectedSongs.value.size
            menu.findItem(R.id.action_edit_song)?.isVisible = selectedCount == 1
        }
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toggle.onOptionsItemSelected(item)) {
            return true
        }
        return when (item.itemId) {
            R.id.action_delete_song -> {
                deleteSelectedSongs()
                true
            }
            R.id.action_edit_song -> {
                editSelectedSong()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun deleteSelectedSongs() {
        AlertDialog.Builder(this)
            .setTitle("Delete Songs")
            .setMessage("Are you sure you want to delete ${viewModel.selectedSongs.value.size} songs?")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deleteSelectedSongs()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun editSelectedSong() {
        viewModel.requestEditForSelectedSong()
    }

    private fun setupUI(view: android.view.View) {
        if (view !is EditText) {
            view.setOnTouchListener { _, _ ->
                getSystemService(InputMethodManager::class.java).hideSoftInputFromWindow(currentFocus?.windowToken, 0)
                currentFocus?.clearFocus()
                false
            }
        }
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                setupUI(view.getChildAt(i))
            }
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_home -> viewPager.currentItem = 0
            R.id.nav_favorites -> startActivity(Intent(this, FavoritesActivity::class.java))
            R.id.nav_settings -> startActivity(Intent(this, SettingsActivity::class.java))
            R.id.nav_share -> shareAppLink()
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun shareAppLink() {
        val shareText = "Check out the LyricsApp! Download it from: [Google Play Store link placeholder]"
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareText)
        }
        startActivity(Intent.createChooser(shareIntent, "Share App via"))
    }

    override fun onBackPressed() {
        when {
            drawerLayout.isDrawerOpen(GravityCompat.START) -> drawerLayout.closeDrawer(GravityCompat.START)
            isInActionMode -> viewModel.clearSelection()
            viewPager.currentItem != 0 -> viewPager.currentItem = 0
            else -> super.onBackPressed()
        }
    }

    override fun onDestroy() {
        viewPager.unregisterOnPageChangeCallback(viewPagerPageChangeCallback)
        super.onDestroy()
    }
}