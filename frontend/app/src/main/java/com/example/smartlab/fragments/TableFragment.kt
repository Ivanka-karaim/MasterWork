package com.example.smartlab.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.smartlab.R
import com.example.smartlab.adapter.MeasurementAdapter
import com.example.smartlab.model.Measurement

class TableFragment : Fragment(R.layout.fragment_table) {
    private lateinit var recycler: RecyclerView
    private var adapter: MeasurementAdapter? = null
    private var pendingData: List<Measurement>? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recycler = view.findViewById(R.id.recycler)
        recycler.layoutManager = LinearLayoutManager(requireContext())
        adapter = MeasurementAdapter(emptyList())
        recycler.adapter = adapter
        pendingData?.let {
            adapter?.setData(it)
            pendingData = null
        }
    }

    fun updateData(measurements: List<Measurement>) {
        if (adapter != null) {
            adapter?.setData(measurements)
        } else {

            pendingData = measurements
        }
    }
}
