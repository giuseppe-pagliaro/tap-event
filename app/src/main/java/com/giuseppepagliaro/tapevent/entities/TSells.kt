package com.giuseppepagliaro.tapevent.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "t_sells",

    primaryKeys = [
        "cashPointEventCod",
        "cashPointName",
        "ticketTypeEventCod",
        "ticketTypeName"
    ],

    foreignKeys = [
        ForeignKey(
            entity = CashPoint::class,
            parentColumns = ["eventCod", "name"],
            childColumns = ["cashPointEventCod", "cashPointName"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = TicketType::class,
            parentColumns = ["eventCod", "name"],
            childColumns = ["ticketTypeEventCod", "ticketTypeName"],
            onDelete = ForeignKey.CASCADE
        )
    ],

    indices = [
        Index(value = ["cashPointEventCod", "cashPointName"], unique = false),
        Index(value = ["ticketTypeEventCod", "ticketTypeName"], unique = false)
    ]
)
data class TSells(
    val cashPointEventCod: Long,
    val cashPointName: String,
    val ticketTypeEventCod: Long,
    val ticketTypeName: String
)
