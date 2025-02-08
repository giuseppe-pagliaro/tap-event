package com.giuseppepagliaro.tapevent

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.giuseppepagliaro.tapevent.models.Selectable
import com.giuseppepagliaro.tapevent.repositories.CashPointRepository
import com.giuseppepagliaro.tapevent.repositories.CustomerRepository
import com.giuseppepagliaro.tapevent.repositories.SelectableRepository
import com.giuseppepagliaro.tapevent.repositories.StandRepository
import com.giuseppepagliaro.tapevent.viewmodels.ItemSelectorFragmentViewModel
import java.math.BigDecimal
import java.math.RoundingMode

class TicketItemSelectorFragment : ItemSelectorFragmentWithRepo(
    CashPointRepository()
) {
    override val addsNewCustomers: Boolean = true
}

class ProductsItemSelectorFragment : ItemSelectorFragmentWithRepo(
    StandRepository()
) {
    override val addsNewCustomers: Boolean = false
}

abstract class ItemSelectorFragmentWithRepo(
    private val selectableRepository: SelectableRepository
) : ItemSelectorFragment() {
    private lateinit var sessionId: String
    private var eventCod: Long = -1

    private lateinit var customerRepository: CustomerRepository

    override fun getViewModelFactory(): ItemSelectorFragmentViewModel.Factory {
        val activity = requireActivity()

        sessionId = arguments?.getString("session_id") ?: run {
            MainActivity.onSessionIdInvalidated(activity)

            // Ritorna un'istanza banale della Factory, perché tanto la view non verrà
            // mai mostrata se si raggiunge questo punto.
            return DummyHelper.getDummyViewModelFactory()
        }
        eventCod = arguments?.getLong("event_cod")
            ?: throw IllegalArgumentException("Event cod needed to start an EventFragment.")

        customerRepository = CustomerRepository()

        return ItemSelectorFragmentViewModel.Factory(
            getString(R.string.event_tickets_title),
            this::getPassphrase,
            { customerRepository.requestNewCustomerId(sessionId) },
            { id -> customerRepository.confirmCustomerId(sessionId, id) },
            { id -> customerRepository.cancelCustomerId(sessionId, id) },
            this::getLocations,
            this::getSelectable,
            { id, items -> selectableRepository.executeTransaction(sessionId, id, items) }
        )
    }

    private fun getPassphrase(): String {
        val passphrase = customerRepository.getCipherPasscode(sessionId)
        if (passphrase == null) {
            MainActivity.onSessionIdInvalidated(requireActivity())
            return ""
        }

        return passphrase
    }

    private fun getLocations(): LiveData<List<String>> {
        val locations = selectableRepository.getAvailableLocations(sessionId, eventCod)
        if (locations == null) {
            MainActivity.onSessionIdInvalidated(requireActivity())
            return MutableLiveData()
        }

        return locations
    }

    private fun getSelectable(location: String): List<Selectable> {
        val selectable = selectableRepository.getSelectable(sessionId, eventCod, location)
        if (selectable == null) {
            MainActivity.onSessionIdInvalidated(requireActivity())
            return listOf()
        }

        return selectable
    }
}

class DummyItemSelectorFragmentWithCustomerCreation : ItemSelectorFragment() {
    override val addsNewCustomers: Boolean = true

    override fun getViewModelFactory(): ItemSelectorFragmentViewModel.Factory =
        DummyHelper.getDummyViewModelFactory()
}

class DummyItemSelectorFragmentNoCustomerCreation : ItemSelectorFragment() {
    override val addsNewCustomers: Boolean = false

    override fun getViewModelFactory(): ItemSelectorFragmentViewModel.Factory =
        DummyHelper.getDummyViewModelFactory()
}

private class DummyHelper {
    companion object {
        private const val LOG_TAG = "DummyItemSelectorVM"

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

        fun getDummyViewModelFactory(): ItemSelectorFragmentViewModel.Factory {
            return ItemSelectorFragmentViewModel.Factory(
                "Selectables",
                { "super_secure_password" },
                { "this_is_an_actual_customer_id" },
                { },
                { },
                { MutableLiveData(locationToSelectable.keys.toList()) },
                { location -> locationToSelectable[location]!! },
                { clientCode, items ->
                    Log.d(LOG_TAG, "Added to client $clientCode: $items")
                    true
                }
            )
        }
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