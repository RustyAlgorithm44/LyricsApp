package com.guruguhan.lyricsapp.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.guruguhan.lyricsapp.R
import com.guruguhan.lyricsapp.SongDetailActivity
import com.guruguhan.lyricsapp.ui.SongAdapter
import com.guruguhan.lyricsapp.viewmodel.SongViewModel
import kotlinx.coroutines.launch

class AllSongsFragment : Fragment() {

    private val viewModel: SongViewModel by activityViewModels()
    private lateinit var adapter: SongAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyStateTextView: TextView

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

        adapter = SongAdapter(
            onItemClick = { song ->
                val intent = Intent(requireActivity(), SongDetailActivity::class.java).apply {
                    putExtra("SONG_ID", song.id)
                }
                startActivity(intent)
            },
            onItemLongClick = { song ->
                // Long click functionality will need to be handled by MainActivity for contextual action mode
                // For now, we'll just open the detail view
                val intent = Intent(requireActivity(), SongDetailActivity::class.java).apply {
                    putExtra("SONG_ID", song.id)
                }
                startActivity(intent)
            }
        )

        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter

        lifecycleScope.launch {
            viewModel.allSongs.collect { songs ->
                adapter.submitList(songs)
                emptyStateTextView.visibility = if (songs.isEmpty()) View.VISIBLE else View.GONE
            }
        }
    }
}
