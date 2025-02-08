package com.giuseppepagliaro.tapevent.users

import android.net.Uri
import com.giuseppepagliaro.tapevent.entities.InternalUser

data class UserInfo(
    val username: String,
    val profilePicture: Uri = InternalUser.DEFAULT_PROPIC_URL
)
