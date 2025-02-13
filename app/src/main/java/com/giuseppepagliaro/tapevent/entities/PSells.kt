package com.giuseppepagliaro.tapevent.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "p_sells",

    primaryKeys = [
        "standEventCod",
        "standName",
        "productEventCod",
        "productName",
        "ticketEventCod",
        "ticketName"
    ],

    foreignKeys = [
        ForeignKey(
            entity = Stand::class,
            parentColumns = ["eventCod", "name"],
            childColumns = ["standEventCod", "standName"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Product::class,
            parentColumns = ["eventCod", "name"],
            childColumns = ["productEventCod", "productName"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = TicketType::class,
            parentColumns = ["eventCod", "name"],
            childColumns = ["ticketEventCod", "ticketName"],
            onDelete = ForeignKey.CASCADE
        )
    ],

    indices = [
        Index(value = ["standEventCod", "standName"], unique = false),
        Index(value = ["productEventCod", "productName"], unique = false),
        Index(value = ["ticketEventCod", "ticketName"], unique = false)
    ]
)
data class PSells(
    val standEventCod: Long,
    val standName: String,

    val productEventCod: Long,
    val productName: String,

    val ticketEventCod: Long,
    val ticketName: String,

    val priceTickets: Int
)
