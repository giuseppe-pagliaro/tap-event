package com.giuseppepagliaro.tapevent.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.giuseppepagliaro.tapevent.models.Selectable
import com.giuseppepagliaro.tapevent.models.Selected
import java.math.BigDecimal
import java.math.RoundingMode

class DummyItemSelectorViewModel : ItemSelectorViewModel() {
    private val logTag = "DummyItemSelectorVM"

    override val selectableName: String = "Selectables"

    override fun getCustomerIdCipherPassphrase(): String = "super_secure_password"
    override fun requestNewCustomerId(): String = "this_is_an_actual_customer_id"
    override fun confirmCustomerId(id: String) { }
    override fun cancelCustomerId(id: String) { }

    companion object {
        private val locationToSelectableCount = mapOf(
            0 to 4,
            1 to 7,
            2 to 5,
            3 to 9
        )

        private val locationToSelectable: Map<String, List<Selectable>> =
            mutableMapOf<String, List<Selectable>>().apply {
                for (i in 1..locationToSelectableCount.size) {
                    val selectableCount = locationToSelectableCount[i - 1]!!

                    this["Location $i"] = MutableList(selectableCount) { j ->
                        val intPart = (1..50).random()
                        val decPart = (1..99).random()

                        DummySelectable("Item ${j + 1}", intPart + decPart / 100f)
                    }.toList()
                }
            }.toMap()
    }

    override fun getAvailableLocationsSource(): LiveData<List<String>> {
        return MutableLiveData(locationToSelectable.keys.toList())
    }

    override fun getSelectable(location: String): List<Selectable> {
        return locationToSelectable[location]!!
    }

    override fun executeTransaction(clientCode: String, items: List<Selected>): Boolean {
        Log.d(logTag, "Added to client $clientCode: $items")
        return true
    }

    private class DummySelectable(
        name: String,
        private val price: Float
    ) : Selectable(name) {
        override fun getPrice(count: Int): String {
            val totalPrice = price * count
            val formatter = BigDecimal(totalPrice.toDouble())
            return "${formatter.setScale(2, RoundingMode.HALF_EVEN)} €"
        }

        override fun toString(): String {
            return "DummySelectable($name, ${getPrice()})"
        }
    }
}