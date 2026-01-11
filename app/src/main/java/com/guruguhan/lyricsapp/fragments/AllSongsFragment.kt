package com.guruguhan.lyricsapp.fragments

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.guruguhan.lyricsapp.AddEditSongActivity
import com.guruguhan.lyricsapp.R
import com.guruguhan.lyricsapp.SongDetailActivity
import com.guruguhan.lyricsapp.ui.SongAdapter
import com.guruguhan.lyricsapp.viewmodel.SongViewModel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class AllSongsFragment : Fragment() {

    private val viewModel: SongViewModel by activityViewModels()
    private lateinit var adapter: SongAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyStateTextView: TextView

    lateinit var editSongLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        editSongLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                viewModel.clearSelection()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_all_songs, container, false)
        recyclerView = view.findViewById(R.id.fragmentSongRecyclerView)
        emptyStateTextView = view.findViewById(R.id.fragmentEmptyStateTextView)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        var isInActionMode = false

        adapter = SongAdapter(
            onItemClick = { song ->
                if (isInActionMode) {
                    viewModel.toggleSongSelection(song)
                } else {
                    val intent = Intent(requireActivity(), SongDetailActivity::class.java).apply {
                        putExtra("SONG_ID", song.id)
                    }
                    startActivity(intent)
                }
            },
            onItemLongClick = { song ->
                viewModel.toggleSongSelection(song)
            }
        )

        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter

        adapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onChanged() {
                super.onChanged()
                checkEmptyState()
            }
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                super.onItemRangeInserted(positionStart, itemCount)
                checkEmptyState()
            }
            override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
                super.onItemRangeRemoved(positionStart, itemCount)
                checkEmptyState()
            }
        })


        lifecycleScope.launch {
            viewModel.allSongs.collect { songs ->
                adapter.setData(songs)
            }
        }

        lifecycleScope.launch {
            viewModel.searchQuery.collect { query ->
                adapter.filter.filter(query)
            }
        }

        lifecycleScope.launch {
            combine(viewModel.isInActionMode, viewModel.selectedSongs) { inActionMode, selectedSongs ->
                isInActionMode = inActionMode
                adapter.selectedSongs = selectedSongs
                adapter.notifyDataSetChanged() // To update backgrounds
            }.collect{}
        }

        lifecycleScope.launch {
            viewModel.editCommand.collect { songToEdit ->
                val intent = Intent(requireActivity(), AddEditSongActivity::class.java).apply {
                    putExtra("SONG_ID", songToEdit.id)
                }
                editSongLauncher.launch(intent)
            }
        }

        // Add OnItemTouchListener to clear selection when clicking on empty space in RecyclerView
        recyclerView.addOnItemTouchListener(object : RecyclerView.OnItemTouchListener {
            override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
                if (viewModel.isInActionMode.value && e.action == MotionEvent.ACTION_UP) {
                    val child = rv.findChildViewUnder(e.x, e.y)
                    if (child == null) { // Click on empty space
                        viewModel.clearSelection()
                        return true // Consume the event
                    }
                }
                return false // Don't intercept
            }
            override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {}
            override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {}
        })
    }

    private fun checkEmptyState() {
        if (adapter.itemCount == 0) {
            if (!viewModel.searchQuery.value.isNullOrBlank()) {
                emptyStateTextView.text = getString(R.string.no_songs_found)
                emptyStateTextView.visibility = View.VISIBLE
            } else {
                emptyStateTextView.text = getString(R.string.no_songs_yet)
                emptyStateTextView.visibility = View.VISIBLE
            }
        } else {
            emptyStateTextView.visibility = View.GONE
        }
    }
}
