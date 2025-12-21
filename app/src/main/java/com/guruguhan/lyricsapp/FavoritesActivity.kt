package com.guruguhan.lyricsapp

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.navigation.NavigationView
import com.guruguhan.lyricsapp.ui.SongAdapter
import com.guruguhan.lyricsapp.viewmodel.SongViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import android.util.TypedValue


class FavoritesActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private val viewModel: SongViewModel by viewModels()
    private lateinit var songAdapter: SongAdapter
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var toggle: ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_favorites)

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

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

        songAdapter = SongAdapter(
            onItemClick = { song ->
                val intent = Intent(this, SongDetailActivity::class.java).apply {
                    putExtra("SONG_ID", song.id)
                }
                startActivity(intent)
            },
            onItemLongClick = {
                // No action mode in favorites for simplicity
            }
        )

        val recyclerView = findViewById<RecyclerView>(R.id.favoritesRecyclerView)
        recyclerView.adapter = songAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        val emptyState = findViewById<TextView>(R.id.emptyStateTextView)

        lifecycleScope.launch {
            viewModel.favoriteSongs.collectLatest { songs ->
                songAdapter.submitList(songs)
                if (songs.isEmpty()) {
                    emptyState.visibility = View.VISIBLE
                    recyclerView.visibility = View.GONE
                } else {
                    emptyState.visibility = View.GONE
                    recyclerView.visibility = View.VISIBLE
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        navigationView.menu.findItem(R.id.nav_favorites).isChecked = true
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_home -> {
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(intent)
                finish()
            }
            R.id.nav_favorites -> {
                // Already in Favorites
                drawerLayout.closeDrawer(GravityCompat.START)
            }
            R.id.nav_settings -> {
                val intent = Intent(this, SettingsActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(intent)
                finish()
            }
            R.id.nav_share -> {
                shareApk()
            }
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun shareApk() {
        Toast.makeText(this, "Link to download the app will be available soon!", Toast.LENGTH_LONG).show()
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}
