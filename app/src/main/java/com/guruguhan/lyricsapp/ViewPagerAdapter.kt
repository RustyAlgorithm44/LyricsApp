package com.guruguhan.lyricsapp

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.guruguhan.lyricsapp.fragments.AllSongsFragment
import com.guruguhan.lyricsapp.fragments.CategoryListFragment
import com.guruguhan.lyricsapp.fragments.ComposerListFragment
import com.guruguhan.lyricsapp.fragments.DeityListFragment

class ViewPagerAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int = 4

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> AllSongsFragment()
            1 -> DeityListFragment()
            2 -> ComposerListFragment()
            3 -> CategoryListFragment()
            else -> throw IllegalArgumentException("Invalid position")
        }
    }
}
