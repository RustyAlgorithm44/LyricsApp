package com.guruguhan.lyricsapp.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.guruguhan.lyricsapp.R
import com.guruguhan.lyricsapp.data.Song
import java.util.*

class ExpandableGroupAdapter(
    private val onSongClick: (Song) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>(), Filterable {

    private var originalGroupedData: List<Pair<String, List<Song>>> = emptyList()
    private var groupedData: List<Pair<String, List<Song>>> = emptyList()
    private val displayList = mutableListOf<DisplayItem>()
    private val expandedGroups = mutableSetOf<String>()

    sealed class DisplayItem {
        data class Group(val name: String) : DisplayItem()
        data class SongItem(val song: Song) : DisplayItem()
    }

    companion object {
        private const val TYPE_GROUP = 0
        private const val TYPE_SONG = 1
    }

    fun submitList(data: Map<String, List<Song>>) {
        expandedGroups.clear() // Clear expanded state on new data
        originalGroupedData = data.entries.sortedBy { it.key }.map { it.key to it.value.sortedBy { s -> s.title } }
        groupedData = originalGroupedData
        rebuildDisplayList()
        notifyDataSetChanged()
    }

    private fun rebuildDisplayList() {
        displayList.clear()
        for ((group, songs) in groupedData) {
            displayList.add(DisplayItem.Group(group))
            if (expandedGroups.contains(group)) {
                songs.forEach { song ->
                    displayList.add(DisplayItem.SongItem(song))
                }
            }
        }
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val filteredData = if (constraint.isNullOrBlank()) {
                    originalGroupedData
                } else {
                    val filterPattern = constraint.toString().lowercase(Locale.getDefault()).trim()
                    originalGroupedData.mapNotNull { (groupName, songs) ->
                        val groupNameMatches = groupName.lowercase(Locale.getDefault()).contains(filterPattern)
                        if (groupNameMatches) {
                            // If group name matches, include all its songs
                            groupName to songs
                        } else {
                            // Otherwise, check if any song in the group matches
                            val matchingSongs = songs.filter { song ->
                                song.title.lowercase(Locale.getDefault()).contains(filterPattern) ||
                                song.composer.lowercase(Locale.getDefault()).contains(filterPattern) ||
                                (song.deity?.lowercase(Locale.getDefault())?.contains(filterPattern) ?: false) ||
                                (song.ragam?.lowercase(Locale.getDefault())?.contains(filterPattern) ?: false)
                            }
                            if (matchingSongs.isNotEmpty()) {
                                groupName to matchingSongs
                            } else {
                                null // Exclude group if nothing matches
                            }
                        }
                    }
                }
                val results = FilterResults()
                results.values = filteredData
                return results
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                groupedData = results?.values as? List<Pair<String, List<Song>>> ?: emptyList()
                rebuildDisplayList()
                notifyDataSetChanged()
            }
        }
    }


    override fun getItemViewType(position: Int): Int {
        return when (displayList[position]) {
            is DisplayItem.Group -> TYPE_GROUP
            is DisplayItem.SongItem -> TYPE_SONG
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_GROUP -> {
                val view = inflater.inflate(R.layout.item_group_expandable, parent, false)
                GroupViewHolder(view)
            }
            TYPE_SONG -> {
                val view = inflater.inflate(R.layout.item_song_indented, parent, false)
                SongViewHolder(view)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = displayList[position]) {
            is DisplayItem.Group -> {
                (holder as GroupViewHolder).bind(item.name, expandedGroups.contains(item.name))
                holder.itemView.setOnClickListener {
                    handleGroupClick(item.name, holder.bindingAdapterPosition)
                }
            }
            is DisplayItem.SongItem -> {
                (holder as SongViewHolder).bind(item.song)
                holder.itemView.setOnClickListener { onSongClick(item.song) }
            }
        }
    }

    private fun handleGroupClick(groupName: String, position: Int) {
        if (expandedGroups.contains(groupName)) {
            // Collapse
            expandedGroups.remove(groupName)
            val songsToRemove = groupedData.find { it.first == groupName }?.second ?: emptyList()
            if (songsToRemove.isNotEmpty()) {
                val start = position + 1
                displayList.subList(start, start + songsToRemove.size).clear()
                notifyItemRangeRemoved(start, songsToRemove.size)
            }
        } else {
            // Expand
            expandedGroups.add(groupName)
            val songsToAdd = groupedData.find { it.first == groupName }?.second ?: emptyList()
            if (songsToAdd.isNotEmpty()) {
                val start = position + 1
                displayList.addAll(start, songsToAdd.map { DisplayItem.SongItem(it) })
                notifyItemRangeInserted(start, songsToAdd.size)
            }
        }
        // Update the arrow indicator on the group item
        notifyItemChanged(position)
    }


    override fun getItemCount(): Int = displayList.size

    inner class GroupViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val groupNameTextView: TextView = itemView.findViewById(R.id.groupName)
        private val indicatorImageView: ImageView = itemView.findViewById(R.id.indicator)

        fun bind(groupName: String, isExpanded: Boolean) {
            groupNameTextView.text = groupName
            indicatorImageView.setImageResource(
                if (isExpanded) R.drawable.ic_arrow_up else R.drawable.ic_arrow_down
            )
        }
    }

    inner class SongViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleTextView: TextView = itemView.findViewById(R.id.songTitle)
        private val composerTextView: TextView = itemView.findViewById(R.id.songComposer)

        fun bind(song: Song) {
            titleTextView.text = song.title
            composerTextView.text = song.composer
        }
    }
}
