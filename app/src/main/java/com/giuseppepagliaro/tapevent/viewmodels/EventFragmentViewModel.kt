package com.giuseppepagliaro.tapevent.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.giuseppepagliaro.tapevent.models.Displayable
import java.util.Date

class EventFragmentViewModel(
    val name: LiveData<String>,
    val date: LiveData<Date>,
    val tickets: LiveData<List<Displayable>>,
    val products: LiveData<List<Displayable>>,

    val getCustomerIdCipherPassphrase: () -> String
): ViewModel() {
    class Factory(
        private val name: LiveData<String>,
        private val date: LiveData<Date>,
        private val tickets: LiveData<List<Displayable>>,
        private val products: LiveData<List<Displayable>>,
        private val getCustomerIdCipherPassphrase: () -> String
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(EventFragmentViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return EventFragmentViewModel(
                    name,
                    date,
                    tickets,
                    products,
                    getCustomerIdCipherPassphrase
                ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}