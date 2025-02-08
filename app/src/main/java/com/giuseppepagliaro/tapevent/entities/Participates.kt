package com.giuseppepagliaro.tapevent.entities

import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    tableName = "participates",

    primaryKeys = ["user", "event"],

    foreignKeys = [
        ForeignKey(
            entity = InternalUser::class,
            parentColumns = ["cod"],
            childColumns = ["user"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Event::class,
            parentColumns = ["cod"],
            childColumns = ["event"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Participates(
    val user: Long,
    val event: Long,

    val isAdmin: Boolean
)
