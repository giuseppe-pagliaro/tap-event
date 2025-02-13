package com.giuseppepagliaro.tapevent.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "product",

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
data class Product(
    val eventCod: Long,
    val name: String,
    val thumbnail: String = DEFAULT_THUMBNAIL_URL
) {
    companion object {
        const val DEFAULT_THUMBNAIL_URL = "android.resource://com.giuseppepagliaro.tapevent/drawable/placeholder"
    }
}