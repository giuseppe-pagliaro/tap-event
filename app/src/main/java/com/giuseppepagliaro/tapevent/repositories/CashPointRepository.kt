package com.giuseppepagliaro.tapevent.repositories

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.giuseppepagliaro.tapevent.models.Selectable
import com.giuseppepagliaro.tapevent.models.Selected
import com.giuseppepagliaro.tapevent.models.TicketInfo

class CashPointRepository : SelectableRepository {
    companion object {
        private val locationToTicketsCount = mapOf(
            0 to 4,
            1 to 7,
            2 to 5,
        )

        private val locationToTickets: Map<String, List<TicketInfo>> =
            mutableMapOf<String, List<TicketInfo>>().apply {
                for (i in 1..locationToTicketsCount.size) {
                    val selectableCount = locationToTicketsCount[i - 1]!!

                    this["Cash Point $i"] = MutableList(selectableCount) { j ->
                        val intPart = (1..50).random()
                        val decPart = (1..99).random()

                        TicketInfo("Item ${j + 1}", intPart + decPart / 100.0)
                    }.toList()
                }
            }.toMap()
    }

    override fun getAvailableLocations(sessionId: String, eventCod: Long): LiveData<List<String>>? {
        return MutableLiveData(locationToTickets.keys.toList())
    }

    override fun getSelectable(
        sessionId: String,
        eventCod: Long,
        locationName: String
    ): List<Selectable>? {
        return locationToTickets[locationName]
    }

    override fun executeTransaction(
        sessionId: String,
        customerId: String,
        items: List<Selected>
    ): Boolean {
        return true
    }
}