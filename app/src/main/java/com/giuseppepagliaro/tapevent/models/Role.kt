package com.giuseppepagliaro.tapevent.models

enum class Role {
    GUEST,
    CASHIER,
    STANDKEEPER,

    // Le persone che sono assegnate sia a qualche stand che a qualche cassa,
    // ma che non sono ORGANIZER.
    MULTITASKER,

    ORGANIZER,
    OWNER
}