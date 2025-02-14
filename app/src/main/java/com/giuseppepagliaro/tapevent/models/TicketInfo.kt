package com.giuseppepagliaro.tapevent.models

import com.giuseppepagliaro.tapevent.entities.TicketType
import java.math.BigDecimal
import java.math.RoundingMode

// Le proprietà sono pubbliche per essere accessibili dal Room ORM.
@Suppress("MemberVisibilityCanBePrivate")
class TicketInfo(name: String, val priceEuros: Double) :
    Selectable(name, name, TicketType.TICKET_THUMBNAIL_URL) {

    override fun getTicketAmount(count: Int): Int = count

    override fun getPriceStr(count: Int): String {
        val totalPriceFormatted = BigDecimal(priceEuros * count)
        return "${ totalPriceFormatted.setScale(2, RoundingMode.HALF_EVEN) } €"
    }
}