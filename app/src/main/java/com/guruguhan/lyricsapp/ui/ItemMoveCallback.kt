package com.guruguhan.lyricsapp.ui

import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView

class ItemMoveCallback(private val adapter: LanguageLyricsAdapter) : ItemTouchHelper.Callback() {

    override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
        val dragFlags = ItemTouchHelper.UP or ItemTouchHelper.DOWN
        return makeMovementFlags(dragFlags, 0)
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        val fromPosition = viewHolder.adapterPosition
        val toPosition = target.adapterPosition
        if (fromPosition < toPosition) {
            for (i in fromPosition until toPosition) {
                java.util.Collections.swap(adapter.items, i, i + 1)
            }
        } else {
            for (i in fromPosition downTo toPosition + 1) {
                java.util.Collections.swap(adapter.items, i, i - 1)
            }
        }
        adapter.notifyItemMoved(fromPosition, toPosition)
        return true
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        // No swipe action
    }

    override fun isLongPressDragEnabled(): Boolean {
        return false
    }

    override fun isItemViewSwipeEnabled(): Boolean {
        return false
    }
}