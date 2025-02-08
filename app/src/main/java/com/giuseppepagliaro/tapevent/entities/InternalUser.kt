package com.giuseppepagliaro.tapevent.entities

import android.net.Uri

data class InternalUser(
    val cod: Int,
    val profilePic: Uri = DEFAULT_PROPIC_URL
) {
    companion object {
        val DEFAULT_PROPIC_URL: Uri = Uri.parse(
            "android.resource://com.giuseppepagliaro.tapevent/drawable/profile"
        )
    }
}
