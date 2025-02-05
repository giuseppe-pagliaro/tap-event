package com.giuseppepagliaro.tapevent

import androidx.lifecycle.MutableLiveData
import com.giuseppepagliaro.tapevent.models.Displayable
import com.giuseppepagliaro.tapevent.viewmodels.EventFragmentViewModel
import java.util.Date

class DummyEventFragment : EventFragment() {
    override fun getViewModelFactory(): EventFragmentViewModel.Factory {
        return EventFragmentViewModel.Factory(
            { MutableLiveData("Event") },
            { MutableLiveData(Date()) },
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
            )) },
            { "super_secure_password" }
        )
    }

}