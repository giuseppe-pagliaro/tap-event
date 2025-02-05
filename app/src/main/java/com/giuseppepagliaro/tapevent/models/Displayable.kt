package com.giuseppepagliaro.tapevent.models

import android.net.Uri

data class Displayable(
    val title: String,
    val soldIn: List<String>,
    val thumbnail: Uri = Uri.parse(
        "android.resource://com.giuseppepagliaro.tapevent/drawable/placeholder"
    )
)
