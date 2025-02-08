package com.giuseppepagliaro.tapevent

import androidx.lifecycle.MutableLiveData
import com.giuseppepagliaro.tapevent.repositories.CustomerRepository
import com.giuseppepagliaro.tapevent.viewmodels.ListActivityViewModel

class TicketsListActivity : ListActivity() {
    override fun getListActivityViewModelFactory(): ListActivityViewModel.Factory {
        val sessionId = intent.getStringExtra("session_id") ?: run {
            MainActivity.onSessionIdInvalidated(this)

            // Ritorna un'istanza banale della Factory, perché tanto la view non verrà
            // mai mostrata se si raggiunge questo punto.
            return ListActivityViewModel.Factory("", MutableLiveData())
        }
        val clientCod = intent.getStringExtra("client_cod")
            ?: throw IllegalArgumentException("Client cod is required")

        val customerRepository = CustomerRepository() // Init

        return ListActivityViewModel.Factory(
            getString(R.string.event_tickets_title),
            customerRepository.getCustomerBalance(sessionId, clientCod)
        )
    }
}