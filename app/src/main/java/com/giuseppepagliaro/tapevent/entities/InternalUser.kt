package com.giuseppepagliaro.tapevent.entities

import android.net.Uri
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "user"
)
data class InternalUser(
    @PrimaryKey
    val cod: Long,
    val profilePic: Uri = DEFAULT_PROPIC_URL
) {
    companion object {
        val DEFAULT_PROPIC_URL: Uri = Uri.parse(
            "android.resource://com.giuseppepagliaro.tapevent/drawable/profile"
        )
    }
}
