package com.giuseppepagliaro.tapevent.entities

import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    tableName = "p_sells",

    primaryKeys = [
        "standEventCod",
        "standNumber",
        "productEventCod",
        "productNumber"
    ],

    foreignKeys = [
        ForeignKey(
            entity = Stand::class,
            parentColumns = ["eventCod", "number"],
            childColumns = ["standEventCod", "standNumber"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Product::class,
            parentColumns = ["eventCod", "number"],
            childColumns = ["productEventCod", "productNumber"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class PSells(
    val standEventCod: Long,
    val standNumber: Int,
    val productEventCod: Long,
    val productNumber: Int
)
