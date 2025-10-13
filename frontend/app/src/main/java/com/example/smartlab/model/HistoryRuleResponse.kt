package com.example.smartlab.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.security.Timestamp

@Parcelize
data class HistoryRuleResponse(
    val dateTime: String?,
    val user: UserProfile?
): Parcelable