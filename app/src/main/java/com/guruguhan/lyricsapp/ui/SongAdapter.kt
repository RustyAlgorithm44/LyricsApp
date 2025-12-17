package com.guruguhan.lyricsapp.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.guruguhan.lyricsapp.R
import com.guruguhan.lyricsapp.data.Song

class SongAdapter : RecyclerView.Adapter<SongAdapter.SongViewHolder>() {

    private var songs: List<Song> = emptyList()

    fun submitList(list: List<Song>) {
        songs = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_song, parent, false)
        return SongViewHolder(view)
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        val song = songs[position]
        holder.title.text = song.title
        holder.artist.text = song.artist
        holder.category.text = song.category
    }

    override fun getItemCount(): Int = songs.size

    class SongViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.titleText)
        val artist: TextView = itemView.findViewById(R.id.artistText)
        val category: TextView = itemView.findViewById(R.id.categoryText)
    }
}