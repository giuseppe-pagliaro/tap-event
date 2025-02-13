package com.giuseppepagliaro.tapevent

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.giuseppepagliaro.tapevent.models.Displayable
import com.giuseppepagliaro.tapevent.repositories.CustomerRepository
import com.giuseppepagliaro.tapevent.viewmodels.ListActivityViewModel

class TicketsListActivity : ListActivity() {
    private lateinit var sessionId: String
    private var eventCod: Long = -1L
    private lateinit var clientCod: String

    private lateinit var customerRepository: CustomerRepository

    override fun getListActivityViewModelFactory(): ListActivityViewModel.Factory {
        sessionId = intent.getStringExtra("session_id") ?: run {
            MainActivity.onSessionIdInvalidated(this)

            // Ritorna un'istanza banale della Factory, perché tanto la view non verrà
            // mai mostrata se si raggiunge questo punto.
            return ListActivityViewModel.Factory("") { MutableLiveData() }
        }
        eventCod = intent.getLongExtra("event_cod", -1)
        if (eventCod == -1L)
            throw IllegalArgumentException("Event cod is required for TicketsListActivity")
        clientCod = intent.getStringExtra("client_cod")
            ?: throw IllegalArgumentException("Client cod is required for TicketsListActivity")

        customerRepository = CustomerRepository(TapEventDatabase.getDatabase(this), eventCod)

        return ListActivityViewModel.Factory(
            getString(R.string.event_tickets_title),
            this::getCustomerBalance
        )
    }

    private suspend fun getCustomerBalance(): LiveData<List<Displayable>> {
        return customerRepository.getCustomerBalance(sessionId, clientCod) ?: run {
            MainActivity.onSessionIdInvalidated(this)
            return MutableLiveData()
        }
    }
}