package com.giuseppepagliaro.tapevent.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.giuseppepagliaro.tapevent.models.Displayable
import java.util.Date

class EventFragmentViewModel(
    getEventName: () -> LiveData<String>,
    getEventDate: () -> LiveData<Date>,
    getTickets: () -> LiveData<List<Displayable>>,
    getProducts: () -> LiveData<List<Displayable>>,

    val getCustomerIdCipherPassphrase: () -> String
): ViewModel() {

    val name: LiveData<String> = getEventName()
    val date: LiveData<Date> = getEventDate()

    val tickets: LiveData<List<Displayable>> = getTickets()
    val products: LiveData<List<Displayable>> = getProducts()

    class Factory(
        private val getEventName: () -> LiveData<String>,
        private val getEventDate: () -> LiveData<Date>,
        private val getTickets: () -> LiveData<List<Displayable>>,
        private val getProducts: () -> LiveData<List<Displayable>>,
        private val getCustomerIdCipherPassphrase: () -> String
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(EventFragmentViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return EventFragmentViewModel(
                    getEventName,
                    getEventDate,
                    getTickets,
                    getProducts,
                    getCustomerIdCipherPassphrase
                ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}