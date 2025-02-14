package com.giuseppepagliaro.tapevent

import android.app.Activity
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.giuseppepagliaro.tapevent.models.Selectable
import com.giuseppepagliaro.tapevent.models.Transaction
import com.giuseppepagliaro.tapevent.models.TransactionResult
import com.giuseppepagliaro.tapevent.repositories.CashPointRepository
import com.giuseppepagliaro.tapevent.repositories.CustomerRepository
import com.giuseppepagliaro.tapevent.repositories.SelectableRepository
import com.giuseppepagliaro.tapevent.repositories.StandRepository
import com.giuseppepagliaro.tapevent.viewmodels.ItemSelectorFragmentViewModel
import com.giuseppepagliaro.tapevent.viewmodels.ProductSelectorFragmentViewModel
import com.giuseppepagliaro.tapevent.viewmodels.TicketSelectorFragmentViewModel
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.reflect.KClass
import kotlin.reflect.full.primaryConstructor

class TicketItemSelectorFragment : ItemSelectorFragment() {
    override val addsNewCustomers: Boolean = true
    override val viewModelType = TicketSelectorFragmentViewModel::class.java

    override fun getViewModelFactory(): TicketSelectorFragmentViewModel.Factory {
        val viewModelProvider = ItemSelectorViewModelProvider(
            requireActivity(),
            CashPointRepository::class,
            arguments
        )

        return TicketSelectorFragmentViewModel.Factory(
            getString(R.string.event_tickets_title),
            viewModelProvider::getPassphrase,
            viewModelProvider::requestNewCustomerId,
            viewModelProvider::confirmCustomerId,
            viewModelProvider::cancelCustomerId,
            viewModelProvider::getLocations,
            viewModelProvider::getSelectable,
            viewModelProvider::executeTransaction
        )
    }
}

class ProductsItemSelectorFragment : ItemSelectorFragment() {
    override val addsNewCustomers: Boolean = false
    override val viewModelType = ProductSelectorFragmentViewModel::class.java

    override fun getViewModelFactory(): ProductSelectorFragmentViewModel.Factory {
        val viewModelProvider = ItemSelectorViewModelProvider(
            requireActivity(),
            StandRepository::class,
            arguments
        )

        return ProductSelectorFragmentViewModel.Factory(
            getString(R.string.event_products_title),
            viewModelProvider::getPassphrase,
            viewModelProvider::getLocations,
            viewModelProvider::getSelectable,
            viewModelProvider::executeTransaction
        )
    }
}

private class ItemSelectorViewModelProvider<T : SelectableRepository>(
    private val activity: Activity,
    selectableRepositoryType: KClass<T>,
    arguments: Bundle?
) {
    private val sessionId: String = arguments?.getString("session_id") ?: run {
        MainActivity.onSessionIdInvalidated(activity)
        ""
    }
    private val eventCod: Long = arguments?.getLong("event_cod")
        ?: throw IllegalArgumentException("Event cod needed to start an EventFragment.")

    private val selectableRepository: SelectableRepository
    private val customerRepository: CustomerRepository

    init {
        val database = TapEventDatabase.getDatabase(activity)
        customerRepository = CustomerRepository(activity, database, eventCod)
        selectableRepository = selectableRepositoryType.primaryConstructor?.call(database, eventCod)
            ?: throw IllegalArgumentException("Invalid Repository Type")
    }

    suspend fun getPassphrase(): String {
        val passphrase = customerRepository.getCipherPassphrase(sessionId)
        if (passphrase == null) {
            MainActivity.onSessionIdInvalidated(activity)
            return ""
        }

        return passphrase
    }

    suspend fun requestNewCustomerId(): String? {
        return customerRepository.requestNewCustomerId(sessionId)
    }

    suspend fun confirmCustomerId(id: String) {
        return customerRepository.confirmCustomerId(sessionId, id)
    }

    suspend fun cancelCustomerId(id: String) {
        return customerRepository.cancelCustomerId(sessionId, id)
    }

    suspend fun getLocations(): LiveData<List<String>> {
        val locations = selectableRepository.getAvailableLocations(sessionId)
        if (locations == null) {
            MainActivity.onSessionIdInvalidated(activity)
            return MutableLiveData()
        }

        return locations
    }

    suspend fun getSelectable(location: String): List<Selectable> {
        val selectable = selectableRepository.getSelectable(sessionId, location)
        if (selectable == null) {
            MainActivity.onSessionIdInvalidated(activity)
            return listOf()
        }

        return selectable
    }

    suspend fun executeTransaction(id: String, items: List<Transaction>): TransactionResult {
        val result = selectableRepository.executeTransactions(sessionId, id, items)
        if (result == TransactionResult.ERROR) {
            MainActivity.onSessionIdInvalidated(activity)
        }

        return result
    }
}

class DummyItemSelectorFragmentWithCustomerCreation : ItemSelectorFragment() {
    override val addsNewCustomers: Boolean = true
    override val viewModelType = ItemSelectorFragmentViewModel::class.java

    override fun getViewModelFactory(): ItemSelectorFragmentViewModel.Factory =
        DummyHelper.getDummyViewModelFactory()
}

class DummyItemSelectorFragmentNoCustomerCreation : ItemSelectorFragment() {
    override val addsNewCustomers: Boolean = false
    override val viewModelType = ItemSelectorFragmentViewModel::class.java

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
                    TransactionResult.OK
                }
            )
        }
    }

    private class DummySelectable(
        name: String,
        private val price: Float
    ) : Selectable(name, "dummy$") {
        override fun getPriceStr(count: Int): String {
            val totalPrice = price * count
            val formatter = BigDecimal(totalPrice.toDouble())
            return "${formatter.setScale(2, RoundingMode.HALF_EVEN)} $currencyName"
        }

        override fun getTicketAmount(count: Int): Int = 1

        override fun toString(): String {
            return "DummySelectable($name, ${getPriceStr()})"
        }
    }
}