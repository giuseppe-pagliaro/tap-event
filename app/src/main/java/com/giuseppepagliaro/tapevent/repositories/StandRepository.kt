package com.giuseppepagliaro.tapevent.repositories

import android.database.sqlite.SQLiteException
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.giuseppepagliaro.tapevent.TapEventDatabase
import com.giuseppepagliaro.tapevent.entities.Owns
import com.giuseppepagliaro.tapevent.entities.PSells
import com.giuseppepagliaro.tapevent.entities.Product
import com.giuseppepagliaro.tapevent.entities.Stand
import com.giuseppepagliaro.tapevent.models.ProductInfo
import com.giuseppepagliaro.tapevent.models.Role
import com.giuseppepagliaro.tapevent.models.Selectable
import com.giuseppepagliaro.tapevent.models.Transaction
import com.giuseppepagliaro.tapevent.models.TransactionResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.HashMap

class StandRepository(
    private val database: TapEventDatabase,
    private val eventCod: Long
) : SelectableRepository {
    private val eventsRepository = EventsRepository(database)

    override suspend fun getAvailableLocations(sessionId: String): LiveData<List<String>>? {
        val stands: LiveData<List<Stand>>?
        withContext(Dispatchers.IO) {
            val role = eventsRepository.getUserRole(sessionId, eventCod)

            when (role) {
                Role.OWNER, Role.ORGANIZER -> {
                    // Dovrebbero ottenere tutti i CashPoint.
                    stands = database.stands().getAll(eventCod)
                }
                Role.MULTI_TASKER, Role.STAND_KEEPER -> {
                    // Dovrebbero ottenere solo i CashPoint a cui sono assegnati.
                    val userCod = database.sessions().getInternalCodBySession(sessionId) ?: run {
                        stands = null
                        return@withContext
                    }

                    stands = database.sManages().getStandsManagedByUser(userCod)
                }
                Role.CASHIER, Role.GUEST, null -> {
                    // Non possono richiedere CashPoint.
                    stands = null
                }
            }
        }

        if (stands == null) return null

        return MediatorLiveData<List<String>>().apply {
            addSource(stands) { stds ->
                if (stds == null) return@addSource

                value = stds.map { it.name }
            }
        }
    }

    override suspend fun getSelectable(
        sessionId: String,
        locationName: String
    ): List<Selectable>? {
        val products: List<ProductInfo>?
        withContext(Dispatchers.IO) {
            val role = eventsRepository.getUserRole(sessionId, eventCod)
            if (role in listOf(Role.CASHIER, Role.GUEST)) {
                products = null
                return@withContext
            }

            products = database.pSells().getByStand(eventCod, locationName)
        }

        return products
    }

    override suspend fun executeTransactions(
        sessionId: String,
        customerId: String,
        items: List<Transaction>
    ): TransactionResult {
        var result: TransactionResult
        withContext(Dispatchers.IO) {
            val role = eventsRepository.getUserRole(sessionId, eventCod)
            if (role in listOf(Role.CASHIER, Role.GUEST)) {
                result = TransactionResult.ERROR
                return@withContext
            }

            val ownedTickets = database.owns().getByCustomer(customerId)
            val ownedTicketsMap = HashMap<String, Int>()
            val upsert = ArrayList<Owns>()
            val delete = ArrayList<Owns>()

            for (ticket in ownedTickets)
                ownedTicketsMap[ticket.name] = ticket.count

            for (transaction in items) {
                val ownedCount = ownedTicketsMap[transaction.currencyName] ?: run {
                    result = TransactionResult.INSUFFICIENT_FUNDS
                    return@withContext
                }

                if (ownedCount < transaction.count) {
                    result = TransactionResult.INSUFFICIENT_FUNDS
                    return@withContext
                }
                else if (ownedCount == transaction.count) {
                    delete.add(Owns(customerId, eventCod, transaction.currencyName, ownedCount))
                }
                else {
                    upsert.add(Owns(customerId, eventCod, transaction.currencyName, ownedCount - transaction.count))
                }
            }

            try {
                database.owns().applyTransaction(upsert, delete)
                result = TransactionResult.OK
            }
            catch (_: Exception) {
                result = TransactionResult.ERROR
            }
        }

        return result
    }

    override suspend fun add(sessionId: String, name: String): Boolean {
        val success: Boolean
        withContext(Dispatchers.IO) {
            val role = eventsRepository.getUserRole(sessionId, eventCod)
            if (role !in listOf(Role.OWNER, Role.ORGANIZER)) {
                success = false
                return@withContext
            }
            try {
                database.stands().insert(Stand(
                    eventCod,
                    name
                )) ?: run {
                    success = false
                    return@withContext
                }
            } catch (_: SQLiteException) {
                success = false
                return@withContext
            }

            success = true
        }

        return success
    }

    suspend fun addProduct(sessionId: String, name: String, thumbnail: Uri): Boolean {
        val success: Boolean
        withContext(Dispatchers.IO) {
            val role = eventsRepository.getUserRole(sessionId, eventCod)
            if (role !in listOf(Role.OWNER, Role.ORGANIZER)) {
                success = false
                return@withContext
            }

            try {
                database.products().insert(Product(
                    eventCod,
                    name,
                    thumbnail.toString()
                )) ?: run {
                    success = false
                    return@withContext
                }
            } catch (_: SQLiteException) {
                success = false
                return@withContext
            }

            success = true
        }

        return success
    }

    suspend fun setPriceInStand(
        sessionId: String,

        productName: String,
        standName: String,
        ticketName: String,

        priceTickets: Int
    ): Boolean {
        val success: Boolean
        withContext(Dispatchers.IO) {
            val role = eventsRepository.getUserRole(sessionId, eventCod)
            if (role !in listOf(Role.OWNER, Role.ORGANIZER)) {
                success = false
                return@withContext
            }

            try {
                database.pSells().insert(PSells(
                    eventCod,
                    standName,
                    eventCod,
                    productName,
                    eventCod,
                    ticketName,
                    priceTickets
                )) ?: run {
                    success = false
                    return@withContext
                }
            } catch (_: SQLiteException) {
                success = false
                return@withContext
            }

            success = true
        }

        return success
    }
}