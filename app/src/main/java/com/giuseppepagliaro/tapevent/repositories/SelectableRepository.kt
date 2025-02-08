package com.giuseppepagliaro.tapevent.repositories

import androidx.lifecycle.LiveData
import com.giuseppepagliaro.tapevent.models.Selectable
import com.giuseppepagliaro.tapevent.models.Selected

interface SelectableRepository {
    fun getAvailableLocations(sessionId: String, eventCod: Long): LiveData<List<String>>?
    fun getSelectable(sessionId: String, eventCod: Long, locationName: String): List<Selectable>?
    fun executeTransaction(sessionId: String, customerId: String, items: List<Selected>): Boolean
}