package com.giuseppepagliaro.tapevent.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "s_manages",

    primaryKeys = ["user", "eventCod", "standName"],

    foreignKeys = [
        ForeignKey(
            entity = InternalUser::class,
            parentColumns = ["cod"],
            childColumns = ["user"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Stand::class,
            parentColumns = ["eventCod", "name"],
            childColumns = ["eventCod", "standName"],
            onDelete = ForeignKey.CASCADE
        )
    ],

    indices = [
        Index(value = ["eventCod", "standName"], unique = false),
        Index(value = ["user"], unique = false),
        Index(value = ["eventCod"], unique = false)
    ]
)
data class SManages(
    val user: Long,
    val eventCod: Long,
    val standName: String
)
