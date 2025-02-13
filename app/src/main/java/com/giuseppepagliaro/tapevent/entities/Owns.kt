package com.giuseppepagliaro.tapevent.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "owns",

    primaryKeys = [
        "customer",
        "ticketTypeEventCod",
        "ticketTypeName"
    ],

    foreignKeys = [
        ForeignKey(
            entity = Customer::class,
            parentColumns = ["id"],
            childColumns = ["customer"],
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
        Index(value = ["customer"], unique = false),
        Index(value = ["ticketTypeEventCod", "ticketTypeName"], unique = false)
    ]
)
data class Owns(
    val customer: String,
    val ticketTypeEventCod: Long,
    val ticketTypeName: String,

    val count: Int
)
