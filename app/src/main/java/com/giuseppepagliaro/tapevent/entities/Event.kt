package com.giuseppepagliaro.tapevent.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "event",

    foreignKeys = [
        ForeignKey(
            entity = InternalUser::class,
            parentColumns = ["cod"],
            childColumns = ["owner"],
            onDelete = ForeignKey.CASCADE
        )
    ],

    indices = [
        Index(value = ["owner"], unique = false)
    ]
)
data class Event(
    @PrimaryKey(autoGenerate = true)
    val cod: Long = 0,

    val name: String,

    val owner: Long,

    // Stored as a Timestamp.
    val date: Long
)
