package com.giuseppepagliaro.tapevent.entities

import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    tableName = "product",

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
data class Product(
    val eventCod: Long,
    val number: Int,
    val name: String
)
