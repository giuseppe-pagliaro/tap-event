package com.giuseppepagliaro.tapevent.entities

data class Ticket(
    val evOwner: Int,
    val evCod: Int,
    val name: String,
    val price: Float
)
