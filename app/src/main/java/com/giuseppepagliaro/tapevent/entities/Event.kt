package com.giuseppepagliaro.tapevent.entities

data class Event(
    val owner: Int,
    val cod: Int,

    val name: String,

    // Stored as a Timestamp.
    val date: Long
)
