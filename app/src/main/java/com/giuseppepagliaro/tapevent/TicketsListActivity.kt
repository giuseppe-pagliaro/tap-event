package com.giuseppepagliaro.tapevent

import androidx.lifecycle.MutableLiveData
import com.giuseppepagliaro.tapevent.models.Displayable
import com.giuseppepagliaro.tapevent.viewmodels.ListActivityViewModel

class TicketsListActivity : ListActivity() {
    override fun getListActivityViewModelFactory(): ListActivityViewModel.Factory {
        val clientCod = intent.getStringExtra("client_cod")
            ?: throw IllegalArgumentException("Client cod is required")

        return ListActivityViewModel.Factory(
            getString(R.string.event_tickets_title),
            MutableLiveData(listOf(
                Displayable("Ticket 1", listOf("Location 1", "Location 2", "Location 3")),
                Displayable("Ticket 2", listOf("Location 1", "Location 2")),
                Displayable("Ticket 3", listOf("Location 1"))
            ))
        )
    }
}