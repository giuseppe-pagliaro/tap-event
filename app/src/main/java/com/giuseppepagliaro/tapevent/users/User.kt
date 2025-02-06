package com.giuseppepagliaro.tapevent.users

import android.net.Uri

data class User(
    val internalCod: Int,

    val username: String,
    val password: String,

    val profilePic: Uri = Uri.parse(
        "android.resource://com.giuseppepagliaro.tapevent/drawable/profile"
    )
)
