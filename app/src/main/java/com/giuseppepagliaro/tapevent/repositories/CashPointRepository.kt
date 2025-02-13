package com.giuseppepagliaro.tapevent.repositories

import android.database.sqlite.SQLiteException
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.giuseppepagliaro.tapevent.TapEventDatabase
import com.giuseppepagliaro.tapevent.entities.CashPoint
import com.giuseppepagliaro.tapevent.entities.Owns
import com.giuseppepagliaro.tapevent.entities.TSells
import com.giuseppepagliaro.tapevent.entities.TicketType
import com.giuseppepagliaro.tapevent.models.Role
import com.giuseppepagliaro.tapevent.models.Selectable
import com.giuseppepagliaro.tapevent.models.TicketInfo
import com.giuseppepagliaro.tapevent.models.Transaction
import com.giuseppepagliaro.tapevent.models.TransactionResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.HashMap

class CashPointRepository(
    private val database: TapEventDatabase,
    private val eventCod: Long
) : SelectableRepository {
    private val eventsRepository = EventsRepository(database)

    override suspend fun getAvailableLocations(sessionId: String): LiveData<List<String>>? {
        val cashPoints: LiveData<List<CashPoint>>?
        withContext(Dispatchers.IO) {
            val role = eventsRepository.getUserRole(sessionId, eventCod)

            when (role) {
                Role.OWNER, Role.ORGANIZER -> {
                    // Dovrebbero ottenere tutti i CashPoint.
                    cashPoints = database.cashPoints().getAll(eventCod)
                }
                Role.MULTI_TASKER, Role.CASHIER -> {
                    // Dovrebbero ottenere solo i CashPoint a cui sono assegnati.
                    val userCod = database.sessions().getInternalCodBySession(sessionId) ?: run {
                        cashPoints = null
                        return@withContext
                    }

                    cashPoints = database.cpManages().getCashPointsManagedByUser(userCod)
                }
                Role.STAND_KEEPER, Role.GUEST, null -> {
                    // Non possono richiedere CashPoint.
                    cashPoints = null
                }
            }
        }

        if (cashPoints == null) return null

        return MediatorLiveData<List<String>>().apply {
            addSource(cashPoints) { cPoints ->
                if (cPoints == null) return@addSource

                value = cPoints.map { it.name }
            }
        }
    }

    override suspend fun getSelectable(
        sessionId: String,
        locationName: String
    ): List<Selectable>? {
        val tickets: List<TicketType>?
        withContext(Dispatchers.IO) {
            val role = eventsRepository.getUserRole(sessionId, eventCod)
            if (role in listOf(Role.STAND_KEEPER, Role.GUEST)) {
                tickets = null
                return@withContext
            }

            tickets = database.tSells().getByCashPoint(eventCod, locationName)
        }

        return tickets?.map { TicketInfo(it.name, it.price.toDouble()) }
    }

    override suspend fun executeTransactions(
        sessionId: String,
        customerId: String,
        items: List<Transaction>
    ): TransactionResult {
        var result: TransactionResult
        withContext(Dispatchers.IO) {
            val role = eventsRepository.getUserRole(sessionId, eventCod)
            if (role in listOf(Role.STAND_KEEPER, Role.GUEST)) {
                result = TransactionResult.ERROR
                return@withContext
            }

            val ownedTickets = database.owns().getByCustomer(customerId)
            val ownedTicketsMap = HashMap<String, Int>()
            val upsert = ArrayList<Owns>()

            for (ticket in ownedTickets)
                ownedTicketsMap[ticket.name] = ticket.count

            for (transaction in items) {
                val count = (ownedTicketsMap[transaction.currencyName] ?: 0) + transaction.count
                upsert.add(Owns(customerId, eventCod, transaction.currencyName, count))
            }

            try {
                database.owns().applyTransaction(upsert, listOf())
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
                database.cashPoints().insert(CashPoint(
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

    suspend fun addTicket(sessionId: String, name: String, price: Float): Boolean {
        val success: Boolean
        withContext(Dispatchers.IO) {
            val role = eventsRepository.getUserRole(sessionId, eventCod)
            if (role !in listOf(Role.OWNER, Role.ORGANIZER)) {
                success = false
                return@withContext
            }

            try {
                database.tickets().insert(TicketType(
                    eventCod,
                    name,
                    price
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

    suspend fun linkTicketToCashPoint(sessionId: String, ticketName: String, cashPointName: String): Boolean {
        val success: Boolean
        withContext(Dispatchers.IO) {
            val role = eventsRepository.getUserRole(sessionId, eventCod)
            if (role !in listOf(Role.OWNER, Role.ORGANIZER)) {
                success = false
                return@withContext
            }

            try {
                database.tSells().insert(TSells(eventCod, cashPointName, eventCod, ticketName)) ?: run {
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