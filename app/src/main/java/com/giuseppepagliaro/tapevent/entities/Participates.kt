package com.giuseppepagliaro.tapevent.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

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
    ],

    indices = [
        Index(value = ["user"], unique = false),
        Index(value = ["event"], unique = false)
    ]
)
data class Participates(
    val user: Long,
    val event: Long,

    val isAdmin: Boolean
)
