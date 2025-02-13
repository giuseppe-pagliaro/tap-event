package com.giuseppepagliaro.tapevent.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "cash_point",

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
data class CashPoint(
    val eventCod: Long,
    val name: String
)
