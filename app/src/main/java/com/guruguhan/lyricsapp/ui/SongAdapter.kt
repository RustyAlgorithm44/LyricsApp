package com.guruguhan.lyricsapp.ui

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.guruguhan.lyricsapp.R
import com.guruguhan.lyricsapp.SongDetailActivity
import com.guruguhan.lyricsapp.data.Song

class SongAdapter(
    private val onItemClick: (Song) -> Unit,
    private val onItemLongClick: (Song) -> Unit
) : ListAdapter<Song, SongAdapter.SongViewHolder>(SongDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_song, parent, false)
        return SongViewHolder(view)
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        val song = getItem(position)
        holder.bind(song, onItemClick, onItemLongClick)
    }

    class SongViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.titleText)
        val composer: TextView = itemView.findViewById(R.id.composerText)
        val deity: TextView = itemView.findViewById(R.id.deityText)
        val category: TextView = itemView.findViewById(R.id.categoryText)

        fun bind(song: Song, onItemClick: (Song) -> Unit, onItemLongClick: (Song) -> Unit) {
            title.text = song.title
            composer.text = song.composer
            deity.text = song.deity ?: ""
            category.text = song.category

            itemView.setOnClickListener {
                // Open SongDetailActivity on item click
                val context = itemView.context
                val intent = Intent(context, SongDetailActivity::class.java).apply {
                    putExtra("title", song.title)
                    putExtra("composer", song.composer)
                    putExtra("deity", song.deity)
                    putExtra("category", song.category)
                    putExtra("lyrics", song.lyrics)
                    putExtra("youtubeLink", song.youtubeLink) // Pass youtubeLink
                }
                context.startActivity(intent)
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