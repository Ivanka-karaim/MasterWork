package com.example.smartlab.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class DeviceSimpleResponse(
    val id: String,
    val title: String?,
    val description: String?,
    val inventoryNumber: String?,
    val type: String?
): Parcelable