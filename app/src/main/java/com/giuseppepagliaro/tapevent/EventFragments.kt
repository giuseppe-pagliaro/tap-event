package com.giuseppepagliaro.tapevent

import android.content.Intent
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.giuseppepagliaro.tapevent.models.Displayable
import com.giuseppepagliaro.tapevent.models.EventInfo
import com.giuseppepagliaro.tapevent.models.Role
import com.giuseppepagliaro.tapevent.repositories.CustomerRepository
import com.giuseppepagliaro.tapevent.repositories.EventsRepository
import com.giuseppepagliaro.tapevent.viewmodels.EventFragmentViewModel
import java.util.Date

class EventFragmentImpl : EventFragment() {
    private lateinit var sessionId: String
    private var eventCod: Long = -1

    private lateinit var eventsRepository: EventsRepository
    private lateinit var clientRepository: CustomerRepository

    override fun getViewModelFactory(): EventFragmentViewModel.Factory {
        val activity = requireActivity()

        sessionId = arguments?.getString("session_id") ?: run {
            MainActivity.onSessionIdInvalidated(activity)

            // Ritorna un'istanza banale della Factory, perché tanto la view non verrà
            // mai mostrata se si raggiunge questo punto.
            return DummyEventFragment.dummyFactory
        }
        eventCod = arguments?.getLong("event_cod")
            ?: throw IllegalArgumentException("Event cod needed to start an EventFragment.")

        eventsRepository = EventsRepository(TapEventDatabase.getDatabase(activity))
        clientRepository = CustomerRepository(activity, TapEventDatabase.getDatabase(activity), eventCod)

        return EventFragmentViewModel.Factory(
            this::getEventInfo,
            this::getTickets,
            this::getProducts,
            this::getCipherPassphrase
        )
    }

    override fun putInfoIntoIntent(intent: Intent) {
        intent.putExtra("session_id", sessionId)
        intent.putExtra("event_cod", eventCod)
    }

    private suspend fun getEventInfo(): LiveData<EventInfo> {
        val event = eventsRepository.getByCod(sessionId, eventCod)
        if (event == null) {
            MainActivity.onSessionIdInvalidated(requireActivity())
            return MutableLiveData()
        }

        return event
    }

    private suspend fun getTickets(): LiveData<List<Displayable>> {
        val tickets = eventsRepository.getTickets(sessionId, eventCod)
        if (tickets == null) {
            MainActivity.onSessionIdInvalidated(requireActivity())
            return MutableLiveData()
        }

        return tickets
    }

    private suspend fun getProducts(): LiveData<List<Displayable>> {
        val products = eventsRepository.getProducts(sessionId, eventCod)
        if (products == null) {
            MainActivity.onSessionIdInvalidated(requireActivity())
            return MutableLiveData()
        }

        return products
    }

    private suspend fun getCipherPassphrase(): String {
        val pass = clientRepository.getCipherPassphrase(sessionId)
        if (pass == null) {
            MainActivity.onSessionIdInvalidated(requireActivity())
            return ""
        }

        return pass
    }
}

class DummyEventFragment : EventFragment() {
    companion object {
        val dummyFactory = EventFragmentViewModel.Factory(
            { MutableLiveData(EventInfo(1, "Event", Date(), Role.OWNER)) },
            { MutableLiveData(listOf(
                Displayable("Ticket 1", listOf("Location 1", "Location 2", "Location 3")),
                Displayable("Ticket 2", listOf("Location 1", "Location 2")),
                Displayable("Ticket 3", listOf("Location 1")),
                Displayable("Ticket 4", listOf("Location 1"))
            )) },
            { MutableLiveData(listOf(
                Displayable("Product 1", listOf("Location 1", "Location 2")),
                Displayable("Product 2", listOf("Location 1", "Location 2")),
                Displayable("Product 3", listOf("Location 1")),
            )) }
        ) { "super_secure_password" }
    }

    override fun getViewModelFactory(): EventFragmentViewModel.Factory {
        return dummyFactory
    }

    override fun putInfoIntoIntent(intent: Intent) { }
}