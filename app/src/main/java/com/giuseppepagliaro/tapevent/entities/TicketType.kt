package com.giuseppepagliaro.tapevent.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    "ticket_type",

    primaryKeys = ["eventCod", "name"],

    foreignKeys = [
        ForeignKey(
            entity = Event::class,
            parentColumns = ["cod"],
            childColumns = ["eventCod"],
            onDelete = ForeignKey.CASCADE
        )
    ],

    indices = [
        Index(value = ["eventCod"], unique = false)
    ]
)
data class TicketType(
    val eventCod: Long,
    val name: String,
    val price: Float
) {
    companion object {
        const val TICKET_THUMBNAIL_URL = "android.resource://com.giuseppepagliaro.tapevent/drawable/avd_ticket"
    }
}
