package com.giuseppepagliaro.tapevent.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "stand",

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
data class Stand(
    val eventCod: Long,
    val name: String
)
