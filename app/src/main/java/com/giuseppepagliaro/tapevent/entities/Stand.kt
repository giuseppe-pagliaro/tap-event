package com.giuseppepagliaro.tapevent.entities

import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    tableName = "stand",

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
data class Stand(
    val eventCod: Long,
    val number: Int,
    val name: String
)
