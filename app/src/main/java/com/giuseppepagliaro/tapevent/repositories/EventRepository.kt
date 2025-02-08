package com.giuseppepagliaro.tapevent.repositories

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.giuseppepagliaro.tapevent.models.EventInfo
import com.giuseppepagliaro.tapevent.entities.Event
import com.giuseppepagliaro.tapevent.entities.Role
import com.giuseppepagliaro.tapevent.models.Displayable
import java.util.Date

class EventRepository {
    fun getAll(sessionId: String): LiveData<List<EventInfo>>? {
        //val userCod = getUserBySession(sessionCode) ?: return null
        //val events = daoGetByUser(userCod)

        val events = listOf(
            Event(0, "Event 1", 0, Date().time),
            Event(1, "Event 2", 0, Date().time),
            Event(2, "Event 3", 0, Date().time),
            Event(3, "Event 4", 0, Date().time),
            Event(4, "Event 5", 0, Date().time),
            Event(5, "Event 6", 0, Date().time),
        )

        return MutableLiveData(events.map { e -> EventInfo(e.cod, e.name, Date(e.date), Role.GUEST) })
    }

    fun getByCod(sessionId: String, eventCod: Long): LiveData<EventInfo> {
        return MutableLiveData(EventInfo(0, "Event Name", Date(), Role.GUEST))
    }

    fun getTickets(sessionId: String, eventCod: Long): LiveData<List<Displayable>>? {
        return MutableLiveData(listOf(
            Displayable("Ticket 1", listOf("Location 1", "Location 2", "Location 3")),
            Displayable("Ticket 2", listOf("Location 1", "Location 2")),
            Displayable("Ticket 3", listOf("Location 1")),
        ))
    }

    fun getProducts(sessionId: String, eventCod: Long): LiveData<List<Displayable>>? {
        return MutableLiveData(listOf(
            Displayable("Product 1", listOf("Location 1", "Location 2")),
            Displayable("Product 2", listOf("Location 1", "Location 2")),
        ))
    }
}