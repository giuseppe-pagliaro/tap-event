package com.giuseppepagliaro.tapevent.entities

import android.net.Uri
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "user"
)
data class InternalUser(
    @PrimaryKey(autoGenerate = true)
    val cod: Long = 0L,
    val profilePic: String = DEFAULT_PROPIC_URL.toString()
) {
    companion object {
        val DEFAULT_PROPIC_URL: Uri = Uri.parse(
            "android.resource://com.giuseppepagliaro.tapevent/drawable/profile"
        )
    }
}
