package com.giuseppepagliaro.tapevent.repositories

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.giuseppepagliaro.tapevent.models.ProductInfo
import com.giuseppepagliaro.tapevent.models.Selectable
import com.giuseppepagliaro.tapevent.models.Selected

class StandRepository : SelectableRepository {
    companion object {
        private val locationToProductsCount = mapOf(
            0 to 4,
            1 to 6,
            2 to 9,
        )

        private val locationToProducts: Map<String, List<ProductInfo>> =
            mutableMapOf<String, List<ProductInfo>>().apply {
                for (i in 1..locationToProductsCount.size) {
                    val selectableCount = locationToProductsCount[i - 1]!!

                    this["Stand $i"] = MutableList(selectableCount) { j ->
                        ProductInfo("Item ${j + 1}", (1..50).random(), "ticket")
                    }.toList()
                }
            }.toMap()
    }

    override fun getAvailableLocations(sessionId: String, eventCod: Long): LiveData<List<String>>? {
        return MutableLiveData(locationToProducts.keys.toList())
    }

    override fun getSelectable(
        sessionId: String,
        eventCod: Long,
        locationName: String
    ): List<Selectable>? {
        return locationToProducts[locationName]
    }

    override fun executeTransaction(
        sessionId: String,
        customerId: String,
        items: List<Selected>
    ): Boolean {
        return true
    }
}