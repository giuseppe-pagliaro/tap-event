package com.giuseppepagliaro.tapevent.entities

import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    tableName = "t_sells",

    primaryKeys = [
        "cashPointEventCod",
        "cashPointNumber",
        "ticketTypeEventCod",
        "ticketTypeNumber"
    ],

    foreignKeys = [
        ForeignKey(
            entity = CashPoint::class,
            parentColumns = ["eventCod", "number"],
            childColumns = ["cashPointEventCod", "cashPointNumber"],
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
data class TSells(
    val cashPointEventCod: Long,
    val cashPointNumber: Int,
    val ticketTypeEventCod: Long,
    val ticketTypeNumber: Int
)
