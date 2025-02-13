package com.giuseppepagliaro.tapevent.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "cp_manages",

    primaryKeys = ["user", "eventCod", "cashPointName"],

    foreignKeys = [
        ForeignKey(
            entity = InternalUser::class,
            parentColumns = ["cod"],
            childColumns = ["user"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = CashPoint::class,
            parentColumns = ["eventCod", "name"],
            childColumns = ["eventCod", "cashPointName"],
            onDelete = ForeignKey.CASCADE
        )
    ],

    indices = [
        Index(value = ["user"], unique = false),
        Index(value = ["eventCod", "cashPointName"], unique = false),
        Index(value = ["eventCod"], unique = false)
    ]
)
data class CPManages(
    val user: Long,
    val eventCod: Long,
    val cashPointName: String
)
