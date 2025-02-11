package com.giuseppepagliaro.tapevent.repositories

import android.database.sqlite.SQLiteConstraintException
import android.database.sqlite.SQLiteException
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.giuseppepagliaro.tapevent.TapEventDatabase
import com.giuseppepagliaro.tapevent.entities.CPManages
import com.giuseppepagliaro.tapevent.models.EventInfo
import com.giuseppepagliaro.tapevent.entities.Event
import com.giuseppepagliaro.tapevent.entities.Participates
import com.giuseppepagliaro.tapevent.entities.Product
import com.giuseppepagliaro.tapevent.entities.SManages
import com.giuseppepagliaro.tapevent.entities.TicketType
import com.giuseppepagliaro.tapevent.models.Role
import com.giuseppepagliaro.tapevent.models.Displayable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Date

class EventRepository(
    private val database: TapEventDatabase
) {
    suspend fun getAll(sessionId: String): LiveData<List<EventInfo>>? {
        val events: LiveData<List<Event>>?
        withContext(Dispatchers.IO) {
            database.sessions().getInternalCodBySession(sessionId) ?: run {
                events = null
                return@withContext
            }

            events = database.events().getAll()
        }

        if (events == null) return null

        return MediatorLiveData<List<EventInfo>>().apply {
            addSource(events) { items ->
                if (items == null) return@addSource

                var ret: List<EventInfo> = listOf()
                CoroutineScope(Dispatchers.IO).launch {
                    ret = items.mapNotNull { it -> eventMap(sessionId, it) }
                }.invokeOnCompletion { CoroutineScope(Dispatchers.Main).launch {
                    value = ret
                } }
            }
        }
    }

    suspend fun getByCod(sessionId: String, eventCod: Long): LiveData<EventInfo>? {
        val event: LiveData<Event>?
        withContext(Dispatchers.IO) {
            database.sessions().getInternalCodBySession(sessionId) ?: run {
                event = null
                return@withContext
            }

            event = database.events().getByCod(eventCod)
        }

        if (event == null) return null

        return MediatorLiveData<EventInfo>().apply {
            addSource(event) { info ->
                if (info == null) return@addSource

                var ret: EventInfo? = null
                CoroutineScope(Dispatchers.IO).launch {
                    ret = eventMap(sessionId, info)
                }.invokeOnCompletion { CoroutineScope(Dispatchers.Main).launch {
                    value = ret
                } }
            }
        }
    }

    suspend fun getTickets(sessionId: String, eventCod: Long): LiveData<List<Displayable>>? {
        val tickets: LiveData<List<TicketType>>?
        withContext(Dispatchers.IO) {
            getUserRole(sessionId, eventCod) ?: run {
                tickets = null
                return@withContext
            }
            tickets = database.tickets().getByEvent(eventCod)
        }

        if (tickets == null) return null

        return MediatorLiveData<List<Displayable>>().apply {
            addSource(tickets) outerSource@ { items ->
                if (items.isNullOrEmpty()) return@outerSource

                var ret: List<Displayable> = listOf()
                CoroutineScope(Dispatchers.IO).launch {

                    // Popolo la lista con un valore di SoldIn temporaneo.
                    ret = items.map { ticket ->
                        Displayable(ticket.name, listOf(), Uri.parse(Product.DEFAULT_THUMBNAIL_URL)) // TODO(thumbnail)
                    }

                    // Per ogni ticket, eseguo la query per ottenere la SoldIn List.
                    for (i in items.indices) {
                        val it = items[i]
                        val cashPoints = database.tSells().getCashpointsThatSellsTicket(it.eventCod, it.number)

                        // Popolo la SoldIn List quando man mano che le query finiscono.
                        addSource(cashPoints) innerSource@ { cPoints ->
                            if (cPoints.isNullOrEmpty()) return@innerSource

                            val original = value?.toMutableList() ?: return@innerSource
                            val originalDisplayable = original[i]
                            original[i] = Displayable(
                                originalDisplayable.title,
                                cPoints.map { cPoint -> cPoint.name },
                                originalDisplayable.thumbnail
                            )
                            CoroutineScope(Dispatchers.Main).launch {
                                value = original
                            }
                        }
                    }
                }.invokeOnCompletion { CoroutineScope(Dispatchers.Main).launch {
                    value = ret
                } }
            }
        }
    }

    suspend fun getProducts(sessionId: String, eventCod: Long): LiveData<List<Displayable>>? {
        val products: LiveData<List<Product>>?
        withContext(Dispatchers.IO) {
            getUserRole(sessionId, eventCod) ?: run {
                products = null
                return@withContext
            }
            products = database.products().getByEvent(eventCod)
        }

        if (products == null) return null

        return MediatorLiveData<List<Displayable>>().apply {
            addSource(products) outerSource@ { items ->
                if (items.isNullOrEmpty()) return@outerSource

                var ret: List<Displayable> = listOf()
                CoroutineScope(Dispatchers.IO).launch {

                    // Popolo la lista con un valore di SoldIn temporaneo.
                    ret = items.map { product ->
                        Displayable(product.name, listOf(), Uri.parse(product.thumbnail))
                    }

                    // Per ogni product, eseguo la query per ottenere la SoldIn List.
                    for (i in items.indices) {
                        val it = items[i]
                        val stands = database.pSells().getStandsThatSellsProduct(it.eventCod, it.number)

                        // Popolo la SoldIn List quando man mano che le query finiscono.
                        addSource(stands) innerSource@ { stds ->
                            if (stds.isNullOrEmpty()) return@innerSource

                            val original = value?.toMutableList() ?: return@innerSource
                            val originalDisplayable = original[i]
                            original[i] = Displayable(
                                originalDisplayable.title,
                                stds.map { stand -> stand.name },
                                originalDisplayable.thumbnail
                            )
                            CoroutineScope(Dispatchers.Main).launch {
                                value = original
                            }
                        }
                    }
                }.invokeOnCompletion { CoroutineScope(Dispatchers.Main).launch {
                    value = ret
                } }
            }
        }
    }

    suspend fun getUserRole(sessionId: String, eventCod: Long): Role? {
        val role: Role?
        withContext(Dispatchers.IO) {
            val internalCod = database.sessions().getInternalCodBySession(sessionId) ?: run {
                role = null
                return@withContext
            }

            role = getUserRoleInternalCod(internalCod, eventCod)
        }

        return role
    }

    suspend fun add(
        sessionId: String,

        name: String,
        date: Date
    ): Boolean {
        val result: Boolean
        withContext(Dispatchers.IO) {
            val internalCod = database.sessions().getInternalCodBySession(sessionId) ?: run {
                result = false
                return@withContext
            }

            database.events().insert(Event(name = name, date = date.time, owner = internalCod)) ?: run {
                result = false
                return@withContext
            }

            result = true
        }

        return result
    }

    suspend fun grantRole(
        sessionId: String,

        eventCod: Long,
        username: String,
        role: Role,

        // Utilizzato solo se si seleziona il ruolo CASHIER o STAND_KEEPER.
        locationNumbers: List<Int>, // Lista Vuota => Seleziona Tutti
    ): Boolean {
        var result: Boolean
        withContext(Dispatchers.IO) {

            // Solo l'Owner e gli Organizer possono manipolare i permessi.
            val requestRole = getUserRole(sessionId, eventCod)
            if (requestRole !in arrayOf(Role.OWNER, Role.ORGANIZER)) {
                result = false
                return@withContext
            }

            val internalCod = database.users().getInternalCodByUsername(username) ?: run {
                result = false
                return@withContext
            }

            try {
                when(role) {
                    Role.GUEST -> {
                        database.participates().upsert(Participates(internalCod, eventCod, false))
                    }
                    Role.CASHIER -> {
                        val cashpoints = locationNumbers.ifEmpty { database.cashPoints().getAllNumbers(eventCod) }

                        for (num in cashpoints)
                            try {
                                database.cpManages().insert(CPManages(internalCod, eventCod, num))
                            } catch (_: SQLiteConstraintException) {
                                /* Se un utente gestisce già la location, ignora. */
                            }
                    }
                    Role.MULTI_TASKER -> {

                        // MULTI_TASKER è un ruolo "di servizio" che serve a rappresentare un
                        // utente assegnato a qualche cassa e qualche stand, ma che non sia ORGANIZER.
                        // NON può essere assegnato.
                        result = false
                        return@withContext
                    }
                    Role.STAND_KEEPER -> {
                        val stands = locationNumbers.ifEmpty { database.stands().getAllNumbers(eventCod) }

                        for (num in stands) {
                            try {
                                database.sManages().insert(SManages(internalCod, eventCod, num))
                            } catch (_: SQLiteConstraintException) {
                                /* Se un utente gestisce già la location, ignora. */
                            }
                        }
                    }
                    Role.ORGANIZER -> {
                        database.participates().upsert(Participates(internalCod, eventCod, true))
                    }
                    Role.OWNER -> {

                        // Un evento non può avere più di un owner.
                        result = false
                        return@withContext
                    }
                }
            } catch (_: SQLiteException) {
                result = false
                return@withContext
            }

            result = true
        }

        return result
    }

    private suspend fun eventMap(sessionId: String, it: Event): EventInfo? {
        val role = getUserRole(sessionId, it.cod) ?: return null
        return EventInfo(it.cod, it.name, Date(it.date), role)
    }

    private suspend fun getUserRoleInternalCod(internalCod: Long, eventCod:Long): Role? {
        if (database.events().isOwner(internalCod, eventCod)) {
            return Role.OWNER
        }

        val status = database.participates().getUserParticipationStatus(internalCod, eventCod)
        return when (status) {
            true -> Role.ORGANIZER
            false -> userRoleDisambiguate(internalCod, eventCod) ?: Role.GUEST
            null -> userRoleDisambiguate(internalCod, eventCod)
        }
    }

    private suspend fun userRoleDisambiguate(userCod: Long, eventCod: Long): Role? {
        val role: Role?
        withContext(Dispatchers.IO) {
            if (database.cpManages().isCashier(userCod, eventCod)) {
                if (database.sManages().isStander(userCod, eventCod)) {
                    role = Role.MULTI_TASKER
                    return@withContext
                }
                role = Role.CASHIER
            } else {
                if (database.sManages().isStander(userCod, eventCod)) {
                    role = Role.STAND_KEEPER
                    return@withContext
                }
                role = null
            }
        }

        return role
    }
}