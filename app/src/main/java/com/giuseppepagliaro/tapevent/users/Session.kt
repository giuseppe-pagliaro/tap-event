package com.giuseppepagliaro.tapevent.users

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "session",

    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["internalCod"],
            childColumns = ["user"],
            ForeignKey.CASCADE
        )
    ],

    indices = [
        Index(value = ["user"], unique = false)
    ]
)
data class Session(
    @PrimaryKey
    val id: String,
    val user: Long
)
