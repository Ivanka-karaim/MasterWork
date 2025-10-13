package com.example.smartlab.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class UserProfile(
    val id: String,
    val fullName: String,
    val email: String,
    val role: String
): Parcelable
