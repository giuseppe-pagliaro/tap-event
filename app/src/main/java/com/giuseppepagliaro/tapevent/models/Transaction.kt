package com.giuseppepagliaro.tapevent.models

data class Transaction(
    val itemName: String,
    val currencyName: String,
    val count: Int
)
