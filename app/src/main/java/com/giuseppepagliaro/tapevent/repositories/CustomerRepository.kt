package com.giuseppepagliaro.tapevent.repositories

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.giuseppepagliaro.tapevent.models.Displayable

class CustomerRepository {
    fun getCipherPasscode(sessionId: String): String? {
        return "super_secure_password"
    }

    fun requestNewCustomerId(sessionId: String): String? {
        return "this_is_an_actual_customer_id"
    }

    fun confirmCustomerId(sessionId: String, id: String) {
        // TODO
    }

    fun cancelCustomerId(sessionId: String, id: String) {
        // TODO
    }

    fun getCustomerBalance(sessionId: String, id: String): LiveData<List<Displayable>> {
        return MutableLiveData(listOf(
            Displayable("Ticket 1", listOf("Location 1", "Location 2", "Location 3")),
            Displayable("Ticket 2", listOf("Location 1", "Location 2")),
            Displayable("Ticket 3", listOf("Location 1"))
        ))
    }
}