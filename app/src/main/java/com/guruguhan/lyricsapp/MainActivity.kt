package com.guruguhan.lyricsapp

import com.guruguhan.lyricsapp.R
import com.guruguhan.lyricsapp.SongDetailActivity
import com.guruguhan.lyricsapp.AddEditSongActivity
import com.guruguhan.lyricsapp.FavoritesActivity
import com.guruguhan.lyricsapp.SettingsActivity
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import android.widget.ArrayAdapter
import android.widget.EditText
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2 // Import ViewPager2
import com.google.android.material.chip.Chip
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.chip.ChipGroup
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.guruguhan.lyricsapp.data.Song
import com.guruguhan.lyricsapp.ui.ExpandableGroupAdapter
import com.guruguhan.lyricsapp.ui.SongAdapter
import android.view.MotionEvent
import com.guruguhan.lyricsapp.viewmodel.SongViewModel
import android.util.TypedValue
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var toggle: ActionBarDrawerToggle

    // SongAdapter and ExpandableGroupAdapter will now be managed by individual fragments
    // private lateinit var adapter: SongAdapter
    // private lateinit var expandableAdapter: ExpandableGroupAdapter
    private lateinit var viewPager: ViewPager2 // Changed from RecyclerView
    private val viewModel: SongViewModel by viewModels()

    private lateinit var editSongLauncher: ActivityResultLauncher<Intent>

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
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Action mode related code will need to be re-evaluated for fragment-based approach
        // For now, let's keep a placeholder or remove it if not critical for initial swipe functionality
        editSongLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    // endManualActionMode() // Re-evaluate action mode handling
                }
            }

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

        viewPager = findViewById(R.id.viewPager)
        viewPager.adapter = ViewPagerAdapter(this)
        viewPager.registerOnPageChangeCallback(viewPagerPageChangeCallback)

        val chipGroup = findViewById<ChipGroup>(R.id.chipGroup)
        chipGroup.setOnCheckedChangeListener { _, checkedId ->
            val position = when (checkedId) {
                R.id.chipAll -> 0
                R.id.chipDeity -> 1
                R.id.chipComposer -> 2
                R.id.chipCategory -> 3
                else -> 0 // Default to All Songs
            }
            if (viewPager.currentItem != position) {
                viewPager.currentItem = position
            }
        }

        // Set initial chip state based on ViewPager current item
        // This is handled by viewPagerPageChangeCallback initially.
        // Or explicitly set here if no initial fragment is set by default
        chipGroup.check(R.id.chipAll) // Default to "All Songs"

        lifecycleScope.launch {
            viewModel.errorEvents.collect { message ->
                Toast.makeText(this@MainActivity, message, Toast.LENGTH_LONG).show()
            }
        }

        val fab = findViewById<FloatingActionButton>(R.id.addSongFab)
        fab.setOnClickListener {
            val intent = Intent(this, AddEditSongActivity::class.java)
            startActivity(intent)
        }

        val searchInput = findViewById<EditText>(R.id.searchInput)
        setupUI(drawerLayout, searchInput)
    }

    private fun setupUI(view: android.view.View, searchInput: EditText) {
        if (view !is EditText) {
            view.setOnTouchListener { _, _ ->
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
                currentFocus?.clearFocus()
                false
            }
        }

        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                val innerView = view.getChildAt(i)
                setupUI(innerView, searchInput)
            }
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_home -> {
                findViewById<Chip>(R.id.chipAll).isChecked = true
                viewPager.currentItem = 0 // Navigate to All Songs
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
        val shareText =
            "Check out the LyricsApp! Download it from: [Google Play Store link placeholder]"
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareText)
        }
        startActivity(Intent.createChooser(shareIntent, "Share App via"))
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else if (viewPager.currentItem != 0) { // Go to "All Songs" view if not already there
            viewPager.currentItem = 0
        } else {
            super.onBackPressed()
        }
    }

    override fun onDestroy() {
        viewPager.unregisterOnPageChangeCallback(viewPagerPageChangeCallback)
        super.onDestroy()
    }
}