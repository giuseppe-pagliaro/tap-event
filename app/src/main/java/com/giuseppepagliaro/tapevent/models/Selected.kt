package com.giuseppepagliaro.tapevent.models

data class Selected(
    val item: Selectable,
    val count: Int
) {
    fun getTotalPrice(): String {
        return item.getPrice(count)
    }
}