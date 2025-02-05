package com.giuseppepagliaro.tapevent.users

data class Session(
    val user: User,
    val sessionCode: String
)
