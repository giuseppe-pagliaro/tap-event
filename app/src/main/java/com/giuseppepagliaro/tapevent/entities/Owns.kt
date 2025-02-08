package com.giuseppepagliaro.tapevent.entities

import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    tableName = "owns",

    primaryKeys = [
        "customerEventCod",
        "customerNumber",
        "ticketTypeEventCod",
        "ticketTypeNumber"
    ],

    foreignKeys = [
        ForeignKey(
            entity = Customer::class,
            parentColumns = ["eventCod", "number"],
            childColumns = ["customerEventCod", "customerNumber"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = TicketType::class,
            parentColumns = ["eventCod", "number"],
            childColumns = ["ticketTypeEventCod", "ticketTypeNumber"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Owns(
    val customerEventCod: Long,
    val customerNumber: Int,
    val ticketTypeEventCod: Long,
    val ticketTypeNumber: Int,

    val count: Int
)
