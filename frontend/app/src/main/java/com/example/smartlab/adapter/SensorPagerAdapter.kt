package com.example.smartlab.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.smartlab.fragments.ChartFragment
import com.example.smartlab.fragments.TableFragment
import com.example.smartlab.model.Measurement

class SensorPagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {
    private val chartFragment = ChartFragment()
    private val tableFragment = TableFragment()
    override fun getItemCount() = 2
    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> chartFragment
            else -> tableFragment
        }
    }

    fun updateData(measurements: List<Measurement>) {
        chartFragment.updateData(measurements)
        tableFragment.updateData(measurements)
    }
}
