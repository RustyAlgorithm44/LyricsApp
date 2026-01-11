package com.guruguhan.lyricsapp.fragments

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
import com.guruguhan.lyricsapp.ui.ExpandableGroupAdapter
import com.guruguhan.lyricsapp.viewmodel.SongViewModel
import kotlinx.coroutines.launch

class CategoryListFragment : Fragment() {

    private val viewModel: SongViewModel by activityViewModels()
    private lateinit var expandableAdapter: ExpandableGroupAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyStateTextView: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_grouped_list, container, false)
        recyclerView = view.findViewById(R.id.fragmentGroupedRecyclerView)
        emptyStateTextView = view.findViewById(R.id.fragmentEmptyStateTextView)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        expandableAdapter = ExpandableGroupAdapter { song ->
            // Open SongDetailActivity for the clicked song
            // This will likely be handled by the MainActivity eventually
        }

        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = expandableAdapter

        lifecycleScope.launch {
            viewModel.songsByCategory.collect { songMap ->
                expandableAdapter.submitList(songMap)
                emptyStateTextView.visibility = if (songMap.isEmpty()) View.VISIBLE else View.GONE
                emptyStateTextView.text = "No Categories found."
            }
        }

        lifecycleScope.launch {
            viewModel.searchQuery.collect { query ->
                expandableAdapter.filter.filter(query)
            }
        }
    }
}