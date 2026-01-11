package com.guruguhan.lyricsapp.ui

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.guruguhan.lyricsapp.R
import android.widget.Filter
import android.widget.Filterable
import com.guruguhan.lyricsapp.data.Song
import java.util.*

class SongAdapter(
    private val onItemClick: (Song) -> Unit,
    private val onItemLongClick: (Song) -> Unit
) : ListAdapter<Song, SongAdapter.SongViewHolder>(SongDiffCallback()), Filterable {

    private var originalList: List<Song> = emptyList()

    val selectedSongs = mutableSetOf<Song>()
    var isInActionMode: Boolean = false

    fun setData(list: List<Song>?) {
        originalList = list ?: emptyList()
        submitList(originalList)
    }

    fun toggleSelection(song: Song) {
        if (selectedSongs.contains(song)) {
            selectedSongs.remove(song)
        } else {
            selectedSongs.add(song)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_song, parent, false)
        return SongViewHolder(view)
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        val song = getItem(position)
        holder.bind(song, onItemClick, onItemLongClick, selectedSongs.contains(song))
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val filteredList = if (constraint.isNullOrBlank()) {
                    originalList
                } else {
                    val filterPattern = constraint.toString().lowercase(Locale.getDefault()).trim()
                    originalList.filter {
                        it.title.lowercase(Locale.getDefault()).contains(filterPattern) ||
                        it.composer.lowercase(Locale.getDefault()).contains(filterPattern) ||
                        (it.deity?.lowercase(Locale.getDefault())?.contains(filterPattern) ?: false) ||
                        (it.ragam?.lowercase(Locale.getDefault())?.contains(filterPattern) ?: false) ||
                        it.lyrics.values.any { lyric -> lyric.lowercase(Locale.getDefault()).contains(filterPattern) }
                    }
                }
                val results = FilterResults()
                results.values = filteredList
                return results
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                submitList(results?.values as? List<Song>)
            }
        }
    }

    class SongViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val title: TextView = itemView.findViewById(R.id.titleText)
        private val composer: TextView = itemView.findViewById(R.id.composerText)
        private val deity: TextView = itemView.findViewById(R.id.deityText)

        fun bind(
            song: Song,
            onItemClick: (Song) -> Unit,
            onItemLongClick: (Song) -> Unit,
            isSelected: Boolean
        ) {
            title.text = song.title
            composer.text = song.composer
            deity.text = song.deity ?: ""

            (itemView as MaterialCardView).isChecked = isSelected

            itemView.setOnClickListener {
                onItemClick(song)
            }
            itemView.setOnLongClickListener {
                onItemLongClick(song)
                true
            }
        }
    }

    private class SongDiffCallback : DiffUtil.ItemCallback<Song>() {
        override fun areItemsTheSame(oldItem: Song, newItem: Song): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Song, newItem: Song): Boolean {
            return oldItem == newItem
        }
    }
}