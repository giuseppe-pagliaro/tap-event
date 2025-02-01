package com.giuseppepagliaro.tapevent.models

import android.net.Uri

abstract class Selectable(
    val name: String,
    val thumbnail: Uri = Uri.parse(
        "android.resource://com.giuseppepagliaro.tapevent/drawable/placeholder"
    )
) {
    abstract fun getPrice(count: Int = 1): String
}
