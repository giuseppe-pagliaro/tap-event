package com.giuseppepagliaro.tapevent.repositories

import androidx.lifecycle.LiveData
import com.giuseppepagliaro.tapevent.models.Selectable
import com.giuseppepagliaro.tapevent.models.Transaction
import com.giuseppepagliaro.tapevent.models.TransactionResult

interface SelectableRepository {
    suspend fun getAvailableLocations(sessionId: String): LiveData<List<String>>?
    suspend fun getSelectable(sessionId: String, locationName: String): List<Selectable>?
    suspend fun executeTransactions(sessionId: String, customerId: String, items: List<Transaction>): TransactionResult
    suspend fun add(sessionId: String, name: String): Boolean
}