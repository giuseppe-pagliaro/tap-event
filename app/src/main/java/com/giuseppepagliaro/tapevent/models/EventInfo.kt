package com.giuseppepagliaro.tapevent.models

import android.content.Context
import android.os.Build
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Parcelize
class EventInfo(
    val cod: Long,
    val name: String,
    private val dateInstant: Date,
    val userRole: Role
) : Parcelable {
    fun getDate(context: Context): String {
        val locale: Locale
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            locale = context.resources.configuration.locales[0]
        } else {
            @Suppress("DEPRECATION")
            locale = context.resources.configuration.locale
        }

        val dateFormatter = SimpleDateFormat("d MMMM yyyy, HH:mm", locale)
        return dateFormatter.format(dateInstant)
    }
}