package com.giuseppepagliaro.tapevent.repositories

import com.giuseppepagliaro.tapevent.dto.EventDto
import com.giuseppepagliaro.tapevent.entities.Event
import java.util.Date

class EventRepository(
    val getUserBySession: (String) -> Int?,
    val daoGetByUser: (Int) -> List<Event>
) {
    fun getByUser(sessionCode: String): List<EventDto>? {
        val userCod = getUserBySession(sessionCode) ?: return null
        val events = daoGetByUser(userCod)

        return events.map { e -> EventDto(e.cod, e.name, Date(e.date)) }
    }
}