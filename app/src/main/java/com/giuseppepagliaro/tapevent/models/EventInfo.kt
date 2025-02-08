package com.giuseppepagliaro.tapevent.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.Date

@Parcelize
data class EventInfo(
    val cod: Long,
    val name: String,
    val date: Date,
    val userRole: Role
) : Parcelable
