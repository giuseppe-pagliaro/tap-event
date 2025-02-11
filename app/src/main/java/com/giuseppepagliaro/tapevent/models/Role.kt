package com.giuseppepagliaro.tapevent.models

enum class Role {
    GUEST,
    CASHIER,
    STAND_KEEPER,

    // Le persone che sono assegnate sia a qualche stand che a qualche cassa,
    // ma che non sono ORGANIZER.
    MULTI_TASKER,

    ORGANIZER,
    OWNER
}