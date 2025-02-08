package com.giuseppepagliaro.tapevent.entities

import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    "ticket_type",

    primaryKeys = ["eventCod", "number"],

    foreignKeys = [
        ForeignKey(
            entity = Event::class,
            parentColumns = ["cod"],
            childColumns = ["eventCod"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class TicketType(
    val eventCod: Long,
    val number: Int,
    val name: String,
    val price: Float
)
