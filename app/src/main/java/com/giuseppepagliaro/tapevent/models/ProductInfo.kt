package com.giuseppepagliaro.tapevent.models

// Le proprietà sono pubbliche per essere accessibili dal Room ORM.
@Suppress("MemberVisibilityCanBePrivate")
class ProductInfo : Selectable {
    val ticketName: String
    val priceTickets: Int

    constructor(name: String, thumbnail: String, ticketName: String, priceTickets: Int) : super(name, ticketName, thumbnail) {
        this.priceTickets = priceTickets
        this.ticketName = ticketName
    }

    constructor(name: String, priceTickets: Int, ticketName: String) : super(name, ticketName) {
        this.priceTickets = priceTickets
        this.ticketName = ticketName
    }

    override fun getPrice(count: Int): String {
        return "${ priceTickets * count } $ticketName"
    }
}