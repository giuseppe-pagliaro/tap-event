package com.giuseppepagliaro.tapevent.models

import android.net.Uri
import java.math.BigDecimal
import java.math.RoundingMode

class TicketInfo : Selectable {
    val priceEuros: Double

    constructor(name: String, priceEuros: Double) : super(name) {
        this.priceEuros = priceEuros
    }

    constructor(name: String, priceEuros: Double, thumbnail: Uri) : super(name, thumbnail) {
        this.priceEuros = priceEuros
    }

    override fun getPrice(count: Int): String {
        val totalPriceFormatted = BigDecimal(priceEuros * count)
        return "${ totalPriceFormatted.setScale(2, RoundingMode.HALF_EVEN) } €"
    }
}