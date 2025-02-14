package com.giuseppepagliaro.tapevent.models

data class Selected(
    val item: Selectable,
    val count: Int
) {
    fun getTotalTicketAmount(): Int {
        return item.getTicketAmount(count)
    }

    fun getTotalPriceStr(): String {
        return item.getPriceStr(count)
    }
}