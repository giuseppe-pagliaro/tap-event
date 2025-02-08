package com.giuseppepagliaro.tapevent.entities

import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    tableName = "s_manages",

    primaryKeys = ["user", "eventCod", "standNumber"],

    foreignKeys = [
        ForeignKey(
            entity = InternalUser::class,
            parentColumns = ["cod"],
            childColumns = ["user"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Stand::class,
            parentColumns = ["eventCod", "number"],
            childColumns = ["eventCod", "standNumber"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class SManages(
    val user: Long,
    val eventCod: Long,
    val standNumber: Int
)
