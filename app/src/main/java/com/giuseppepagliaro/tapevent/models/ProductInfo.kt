package com.giuseppepagliaro.tapevent.models

import android.net.Uri

class ProductInfo : Selectable {
    val ticketName: String
    val priceTickets: Int

    constructor(name: String, priceTickets: Int, ticketName: String) : super(name) {
        this.priceTickets = priceTickets
        this.ticketName = ticketName
    }

    constructor(name: String, priceTickets: Int, ticketName: String, thumbnail: Uri) : super(name, thumbnail) {
        this.priceTickets = priceTickets
        this.ticketName = ticketName
    }

    override fun getPrice(count: Int): String {
        return "${ priceTickets * count } $ticketName"
    }
}