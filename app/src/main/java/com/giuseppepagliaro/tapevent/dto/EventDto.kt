package com.giuseppepagliaro.tapevent.dto

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.Date

@Parcelize
data class EventDto(
    val cod: Int,
    val name: String,
    val date: Date
) : Parcelable
