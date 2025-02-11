package com.giuseppepagliaro.tapevent.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "user"
)
data class InternalUser(
    @PrimaryKey(autoGenerate = true)
    val cod: Long = 0L,
    val profilePic: String = DEFAULT_PROPIC_URL
) {
    companion object {
        const val DEFAULT_PROPIC_URL = "android.resource://com.giuseppepagliaro.tapevent/drawable/profile"
    }
}
