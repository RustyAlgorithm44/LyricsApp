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
import com.guruguhan.lyricsapp.R
import com.guruguhan.lyricsapp.data.Song

class SongAdapter(
    private val onItemClick: (Song) -> Unit,
    private val onItemLongClick: (Song) -> Unit
) : ListAdapter<Song, SongAdapter.SongViewHolder>(SongDiffCallback()) {

    val selectedSongs = mutableSetOf<Song>()
    var isInActionMode: Boolean = false

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

    class SongViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val title: TextView = itemView.findViewById(R.id.titleText)
        private val composer: TextView = itemView.findViewById(R.id.composerText)
        private val deity: TextView = itemView.findViewById(R.id.deityText)
        private val category: TextView = itemView.findViewById(R.id.categoryText)

        fun bind(
            song: Song,
            onItemClick: (Song) -> Unit,
            onItemLongClick: (Song) -> Unit,
            isSelected: Boolean
        ) {
            title.text = song.title
            composer.text = song.composer
            deity.text = song.deity ?: ""
            category.text = song.category

            itemView.setBackgroundColor(
                if (isSelected) ContextCompat.getColor(itemView.context, R.color.selection_color)
                else Color.TRANSPARENT
            )

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