package com.example.smartlab.addResources

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class MeasurementFilters(
    val from: String? = null,
    val to: String? = null,
    val minValue: Double? = null,
    val maxValue: Double? = null
) : Parcelable
