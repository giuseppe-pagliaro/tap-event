package com.giuseppepagliaro.tapevent.repositories

import android.database.sqlite.SQLiteException
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.giuseppepagliaro.tapevent.TapEventDatabase
import com.giuseppepagliaro.tapevent.entities.Customer
import com.giuseppepagliaro.tapevent.entities.Product
import com.giuseppepagliaro.tapevent.models.Displayable
import com.giuseppepagliaro.tapevent.models.Role
import com.giuseppepagliaro.tapevent.models.TicketCount
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.security.KeyStore
import java.security.PrivateKey
import java.security.SecureRandom
import java.util.UUID
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

class CustomerRepository(
    private val database: TapEventDatabase,
    private val eventCod: Long
) {
    companion object {
        private const val PASSCODE_ALIAS = "com.giuseppepagliaro.tapevent.passcode"
    }

    private val eventsRepository = EventsRepository(database)

    suspend fun getCipherPassphrase(sessionId: String): String? {
        val passcode: String?

        withContext(Dispatchers.IO) {
            database.sessions().getInternalCodBySession(sessionId) ?: run {
                passcode = null
                return@withContext
            }

            // Inizializza il Keystore
            val keyStore = KeyStore.getInstance("AndroidKeyStore")
            keyStore.load(null)

            // Tenta di ottenere la passphrase salvata nel Keystore
            val existingKey = keyStore.getKey(PASSCODE_ALIAS, null) as SecretKey?

            // Se la passphrase esiste già, la restituisce come stringa
            if (existingKey != null) {
                passcode = String(existingKey.encoded)
                return@withContext
            }

            // Se la passphrase non esiste, la creiamo
            val secureRandom = SecureRandom()
            val randomBytes = ByteArray(32)   // Generiamo una passphrase di 256 bit
            secureRandom.nextBytes(randomBytes)

            // Memorizziamo la passphrase nel Keystore come una chiave segreta
            val keyGenerator = KeyGenerator.getInstance("AES", "AndroidKeyStore")
            keyGenerator.init(256) // 256-bit chiave AES
            val key = keyGenerator.generateKey()

            // Salviamo la passphrase nel Keystore con l'alias
            keyStore.setEntry(PASSCODE_ALIAS, KeyStore.PrivateKeyEntry(key as PrivateKey, null), null)

            // Restituiamo la passphrase come stringa
            passcode = String(randomBytes)
        }

        return passcode
    }

    suspend fun requestNewCustomerId(sessionId: String): String? {
        var customerId: String?

        withContext(Dispatchers.IO) {
            val role = eventsRepository.getUserRole(sessionId, eventCod)

            if (role !in listOf(Role.OWNER, Role.ORGANIZER, Role.MULTITASKER, Role.CASHIER)) {

                // Non si dispone dei permessi necessari.
                customerId = null
                return@withContext
            }

            try {
                customerId = UUID.randomUUID().toString()
                database.customers().upsert(Customer(customerId as String, eventCod, false))
            } catch (_: SQLiteException) {
                customerId = null
                return@withContext
            }
        }

        return customerId
    }

    suspend fun confirmCustomerId(sessionId: String, id: String) {
        withContext(Dispatchers.IO) {
            val role = eventsRepository.getUserRole(sessionId, eventCod)

            if (role !in listOf(Role.OWNER, Role.ORGANIZER, Role.MULTITASKER, Role.CASHIER)) {

                // Non si dispone dei permessi necessari.
                return@withContext
            }

            database.customers().upsert(Customer(id, eventCod, true))
        }
    }

    suspend fun cancelCustomerId(sessionId: String, id: String) {
        withContext(Dispatchers.IO) {
            val role = eventsRepository.getUserRole(sessionId, eventCod)

            if (role !in listOf(Role.OWNER, Role.ORGANIZER, Role.MULTITASKER, Role.CASHIER)) {

                // Non si dispone dei permessi necessari.
                return@withContext
            }

            database.customers().delete(Customer(id, eventCod, false))
        }
    }

    suspend fun getCustomerBalance(sessionId: String, id: String): LiveData<List<Displayable>>? {
        val ownedTickets: LiveData<List<TicketCount>>?
        withContext(Dispatchers.IO) {
            database.sessions().getInternalCodBySession(sessionId) ?: run {
                ownedTickets = null
                return@withContext
            }

            ownedTickets = database.owns().getByCustomerLive(id)
        }

        if (ownedTickets == null) return null

        return MediatorLiveData<List<Displayable>>().apply {
            addSource(ownedTickets) outerSource@ { owns ->
                if (owns == null) return@outerSource

                var ret: List<Displayable> = listOf()
                CoroutineScope(Dispatchers.IO).launch {

                    // Popolo la lista con un valore di SoldIn temporaneo.
                    ret = owns.map { own ->
                        Displayable(
                            if (own.count > 1) "${own.name} x ${own.count}" else own.name,
                            listOf(),
                            Uri.parse(Product.DEFAULT_THUMBNAIL_URL)  // TODO(ticket thumbnail provider)
                        )
                    }

                    // Per ogni ticket, eseguo la query per ottenere la SoldIn List.
                    for (i in owns.indices) {
                        val it = owns[i]
                        val cashPoints = database.tSells().getCashPointsThatSellsTicket(it.eventCod, it.name)

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
}