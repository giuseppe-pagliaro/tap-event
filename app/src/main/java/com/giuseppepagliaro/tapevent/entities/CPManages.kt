package com.giuseppepagliaro.tapevent.entities

import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    tableName = "cp_manages",

    primaryKeys = ["user", "eventCod", "cashPointNumber"],

    foreignKeys = [
        ForeignKey(
            entity = InternalUser::class,
            parentColumns = ["cod"],
            childColumns = ["user"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = CashPoint::class,
            parentColumns = ["eventCod", "number"],
            childColumns = ["eventCod", "cashPointNumber"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class CPManages(
    val user: Long,
    val eventCod: Long,
    val cashPointNumber: Int
)
