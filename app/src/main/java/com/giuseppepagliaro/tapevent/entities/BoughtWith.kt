package com.giuseppepagliaro.tapevent.entities

import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    tableName = "bought_with",

    primaryKeys = [
        "productEventCod",
        "productNumber",
        "ticketTypeEventCod",
        "ticketTypeNumber"
    ],

    foreignKeys = [
        ForeignKey(
            entity = Product::class,
            parentColumns = ["eventCod", "number"],
            childColumns = ["productEventCod", "productNumber"],
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
data class BoughtWith(
    val productEventCod: Long,
    val productNumber: Int,
    val ticketTypeEventCod: Long,
    val ticketTypeNumber: Int,

    val price: Int
)
