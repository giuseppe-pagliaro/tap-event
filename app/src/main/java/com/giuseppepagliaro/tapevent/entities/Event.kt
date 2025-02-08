package com.giuseppepagliaro.tapevent.entities

data class Event(
    val cod: Long,

    val name: String,

    val owner: Int,

    // Stored as a Timestamp.
    val date: Long
)
