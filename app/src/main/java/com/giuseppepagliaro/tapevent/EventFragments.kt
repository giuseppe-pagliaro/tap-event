package com.giuseppepagliaro.tapevent

import android.content.Intent
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.giuseppepagliaro.tapevent.models.Displayable
import com.giuseppepagliaro.tapevent.repositories.CustomerRepository
import com.giuseppepagliaro.tapevent.repositories.EventRepository
import com.giuseppepagliaro.tapevent.viewmodels.EventFragmentViewModel
import java.util.Date

class EventFragmentImpl : EventFragment() {
    private lateinit var sessionId: String
    private var eventCod: Long = -1

    private lateinit var eventRepository: EventRepository
    private lateinit var clientRepository: CustomerRepository

    override suspend fun getViewModelFactory(): EventFragmentViewModel.Factory {
        val activity = requireActivity()

        sessionId = arguments?.getString("session_id") ?: run {
            MainActivity.onSessionIdInvalidated(activity)

            // Ritorna un'istanza banale della Factory, perché tanto la view non verrà
            // mai mostrata se si raggiunge questo punto.
            return DummyEventFragment.dummyFactory
        }
        eventCod = arguments?.getLong("event_cod")
            ?: throw IllegalArgumentException("Event cod needed to start an EventFragment.")

        eventRepository = EventRepository(TapEventDatabase.getDatabase(activity))
        clientRepository = CustomerRepository() // TODO init

        val event = eventRepository.getByCod(sessionId, eventCod) ?: run {
            MainActivity.onSessionIdInvalidated(activity)
            return DummyEventFragment.dummyFactory
        }
        val name = MediatorLiveData<String>().apply {
            addSource(event) { event ->
                value = event.name
            }
        }
        val date = MediatorLiveData<Date>().apply {
            addSource(event) { event ->
                value = event.date
            }
        }

        return EventFragmentViewModel.Factory(
            name,
            date,
            getTickets(),
            getProducts(),
            this::getCipherPassphrase
        )
    }

    override fun putSessionIdIntoIntent(intent: Intent) {
        intent.putExtra("session_id", sessionId)
    }

    private suspend fun getTickets(): LiveData<List<Displayable>> {
        val tickets = eventRepository.getTickets(sessionId, eventCod)
        if (tickets == null) {
            MainActivity.onSessionIdInvalidated(requireActivity())
            return MutableLiveData()
        }

        return tickets
    }

    private suspend fun getProducts(): LiveData<List<Displayable>> {
        val products = eventRepository.getProducts(sessionId, eventCod)
        if (products == null) {
            MainActivity.onSessionIdInvalidated(requireActivity())
            return MutableLiveData()
        }

        return products
    }

    private fun getCipherPassphrase(): String {
        val pass = clientRepository.getCipherPasscode(sessionId)
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
            MutableLiveData("Event"),
            MutableLiveData(Date()),
            MutableLiveData(listOf(
                Displayable("Ticket 1", listOf("Location 1", "Location 2", "Location 3")),
                Displayable("Ticket 2", listOf("Location 1", "Location 2")),
                Displayable("Ticket 3", listOf("Location 1")),
                Displayable("Ticket 4", listOf("Location 1"))
            )),
            MutableLiveData(listOf(
                Displayable("Product 1", listOf("Location 1", "Location 2")),
                Displayable("Product 2", listOf("Location 1", "Location 2")),
                Displayable("Product 3", listOf("Location 1")),
            ))
        ) { "super_secure_password" }
    }

    override suspend fun getViewModelFactory(): EventFragmentViewModel.Factory {
        return dummyFactory
    }

    override fun putSessionIdIntoIntent(intent: Intent) { }
}