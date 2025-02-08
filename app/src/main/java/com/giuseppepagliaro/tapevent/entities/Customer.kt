package com.giuseppepagliaro.tapevent.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    "customer",

    primaryKeys = ["eventCod", "number"],

    foreignKeys = [
        ForeignKey(
            entity = Event::class,
            parentColumns = ["cod"],
            childColumns = ["eventCod"],
            onDelete = ForeignKey.CASCADE
        )
    ],

    indices = [
        Index(value = ["id"], unique = true)
    ]
)
data class Customer(
    val eventCod: Long,
    val number: Int,
    val id: String
)
