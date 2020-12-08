package com.laurencerawlings.pollen.adapter

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.laurencerawlings.pollen.R
import com.laurencerawlings.pollen.ui.main.MainFragment

class MainTabAdapter(private val context: Context, fm: FragmentManager) :
    FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    private val tabTitles = arrayOf(
        R.string.activity_main_tab_title_2,
        R.string.activity_main_tab_title_1,
        R.string.activity_main_tab_title_3
    )

    override fun getItem(position: Int): Fragment {
        return MainFragment.newInstance(position)
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return context.resources.getString(tabTitles[position])
    }

    override fun getCount(): Int {
        return tabTitles.size
    }
}