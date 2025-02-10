package com.giuseppepagliaro.tapevent.users

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "user_credentials",

    indices = [
        Index(value = ["username"], unique = true)
    ]
)
data class User(
    @PrimaryKey
    val internalCod: Long,

    val username: String,
    val password: String
)
