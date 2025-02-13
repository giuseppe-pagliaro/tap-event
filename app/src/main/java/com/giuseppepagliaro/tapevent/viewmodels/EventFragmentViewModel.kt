package com.giuseppepagliaro.tapevent.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.giuseppepagliaro.tapevent.models.Displayable
import com.giuseppepagliaro.tapevent.models.EventInfo
import kotlinx.coroutines.runBlocking

class EventFragmentViewModel(
    private val getEventInfo: suspend () -> LiveData<EventInfo>,
    private val getTickets: suspend () -> LiveData<List<Displayable>>,
    private val getProducts: suspend () -> LiveData<List<Displayable>>,

    val getCustomerIdCipherPassphrase: suspend () -> String
): ViewModel() {
    val eventInfo: LiveData<EventInfo>
    val tickets: LiveData<List<Displayable>>
    val products: LiveData<List<Displayable>>

    init {
        val eventInfo: LiveData<EventInfo>
        val tickets: LiveData<List<Displayable>>
        val products: LiveData<List<Displayable>>

        runBlocking {
            eventInfo = getEventInfo()
            tickets = getTickets()
            products = getProducts()
        }

        this.eventInfo = eventInfo
        this.tickets = tickets
        this.products = products
    }

    class Factory(
        private val getEventInfo: suspend () -> LiveData<EventInfo>,
        private val getTickets: suspend () -> LiveData<List<Displayable>>,
        private val getProducts: suspend () -> LiveData<List<Displayable>>,
        private val getCustomerIdCipherPassphrase: suspend () -> String
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(EventFragmentViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return EventFragmentViewModel(
                    getEventInfo,
                    getTickets,
                    getProducts,
                    getCustomerIdCipherPassphrase
                ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}