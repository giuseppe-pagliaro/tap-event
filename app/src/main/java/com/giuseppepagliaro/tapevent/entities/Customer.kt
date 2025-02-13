package com.giuseppepagliaro.tapevent.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    "customer",

    foreignKeys = [
        ForeignKey(
            entity = Event::class,
            parentColumns = ["cod"],
            childColumns = ["event"],
            onDelete = ForeignKey.CASCADE
        )
    ],

    indices = [
        Index(value = ["event"], unique = false)
    ]
)
data class Customer(
    @PrimaryKey
    val id: String,

    val event: Long,
    val isConfirmed: Boolean
)
