package com.giuseppepagliaro.tapevent

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.giuseppepagliaro.tapevent.daos.BoughtWithDao
import com.giuseppepagliaro.tapevent.daos.CashPointDao
import com.giuseppepagliaro.tapevent.daos.CpManagesDao
import com.giuseppepagliaro.tapevent.daos.EventDao
import com.giuseppepagliaro.tapevent.daos.InternalUserDao
import com.giuseppepagliaro.tapevent.daos.OwnsDao
import com.giuseppepagliaro.tapevent.daos.PSellsDao
import com.giuseppepagliaro.tapevent.daos.ParticipatesDao
import com.giuseppepagliaro.tapevent.daos.ProductDao
import com.giuseppepagliaro.tapevent.daos.SManagesDao
import com.giuseppepagliaro.tapevent.daos.StandDao
import com.giuseppepagliaro.tapevent.daos.TSellsDao
import com.giuseppepagliaro.tapevent.daos.TicketTypeDao
import com.giuseppepagliaro.tapevent.entities.BoughtWith
import com.giuseppepagliaro.tapevent.entities.CPManages
import com.giuseppepagliaro.tapevent.entities.CashPoint
import com.giuseppepagliaro.tapevent.entities.Customer
import com.giuseppepagliaro.tapevent.entities.Event
import com.giuseppepagliaro.tapevent.entities.InternalUser
import com.giuseppepagliaro.tapevent.entities.Owns
import com.giuseppepagliaro.tapevent.entities.PSells
import com.giuseppepagliaro.tapevent.entities.Participates
import com.giuseppepagliaro.tapevent.entities.Product
import com.giuseppepagliaro.tapevent.entities.SManages
import com.giuseppepagliaro.tapevent.entities.Stand
import com.giuseppepagliaro.tapevent.entities.TSells
import com.giuseppepagliaro.tapevent.entities.TicketType
import com.giuseppepagliaro.tapevent.models.Role
import com.giuseppepagliaro.tapevent.repositories.EventRepository
import com.giuseppepagliaro.tapevent.users.Session
import com.giuseppepagliaro.tapevent.users.SessionDao
import com.giuseppepagliaro.tapevent.users.User
import com.giuseppepagliaro.tapevent.users.UserDao
import com.giuseppepagliaro.tapevent.users.UserRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.util.Date

@Database(
    entities = [
        BoughtWith::class,
        CashPoint::class,
        CPManages::class,
        Customer::class,
        Event::class,
        InternalUser::class,
        Owns::class,
        Participates::class,
        Product::class,
        PSells::class,
        SManages::class,
        Stand::class,
        TicketType::class,
        TSells::class,
        User::class,
        Session::class
    ],

    version = 1
)
abstract class TapEventDatabase : RoomDatabase() {
    abstract fun internalUsers(): InternalUserDao
    abstract fun events(): EventDao
    abstract fun tickets(): TicketTypeDao
    abstract fun products(): ProductDao
    abstract fun cashPoints(): CashPointDao
    abstract fun stands(): StandDao

    abstract fun boughtWith(): BoughtWithDao
    abstract fun cpManages(): CpManagesDao
    abstract fun owns(): OwnsDao
    abstract fun participates(): ParticipatesDao
    abstract fun pSells(): PSellsDao
    abstract fun sManages(): SManagesDao
    abstract fun tSells(): TSellsDao

    abstract fun users(): UserDao
    abstract fun sessions(): SessionDao

    companion object {
        private const val DB_NAME = "tap_event"

        @Volatile
        private var INSTANCE: TapEventDatabase? = null

        fun getDatabase(context: Context): TapEventDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TapEventDatabase::class.java,
                    DB_NAME

                // Viene chiamata dopo che il database è creato.
                ).addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)

                        Log.d("DB Callback", "Prepopulating the database")
                        val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
                        coroutineScope.launch {
                            populateDisplayData(context, getDatabase(context))
                        }.invokeOnCompletion {
                            coroutineScope.cancel()
                        }
                    }

                }).build()

                Log.d("DB Create", "Db was created")
                INSTANCE = instance
                instance
            }
        }

        private suspend fun populateDisplayData(context: Context, db: TapEventDatabase) {
            val userRepository = UserRepository(context, db)
            val eventRepository = EventRepository(db)

            // Aggiungi User

            var success = userRepository.add("Giuseppe Pagliaro", "pass")
            Log.d("DB Prepopulate", "Created Giuseppe Pagliaro: $success")

            success = userRepository.add("Giovanni Verdi", "pass")
            Log.d("DB Prepopulate", "Created Giovanni Verdi: $success")

            success = userRepository.add("Malcom Smith", "pass")
            Log.d("DB Prepopulate", "Created Malcom Smith: $success")

            success = userRepository.add("Alberto Toscano", "pass")
            Log.d("DB Prepopulate", "Created Alberto Toscano: $success")

            success = userRepository.add("KIOSK", "pass")
            Log.d("DB Prepopulate", "Created KIOSK: $success")

            // Login

            val giuseppeSessionId = userRepository.login("Giuseppe Pagliaro", "pass", false) ?: run {
                Log.e("DB Prepopulate", "Login of Giuseppe Pagliaro Failed, can't finish the pre-population")
                return
            }
            Log.d("DB Prepopulate", "Login of Giuseppe Pagliaro, success")
            val giovanniSessionId = userRepository.login("Giovanni Verdi", "pass", false) ?: run {
                Log.e("DB Prepopulate", "Login of Giovanni Verdi Failed, can't finish the pre-population")
                return
            }
            Log.d("DB Prepopulate", "Login of Giovanni Verdi, success")

            // Aggiungi Eventi

            success = eventRepository.add(giuseppeSessionId, "Festa del Vino 2001", Date(995209200000))
            Log.d("DB Prepopulate", "Created \"Festa del Vino 2001\" Event: $success")
            success = eventRepository.add(giovanniSessionId, "Festa del Pane 2077", Date(3400936200000))
            Log.d("DB Prepopulate", "Created \"Festa del Pane 2077\" Event: $success")
            val vinoEventCod = 1L
            val paneEventCod = 2L

            // Imposta Ruoli

            success = eventRepository.grantRole(giuseppeSessionId, vinoEventCod, "KIOSK", Role.GUEST, listOf())
            Log.d("DB Prepopulate", "Role.GUEST granted to KIOSK by Giuseppe Pagliaro in Event $vinoEventCod, success: $success")
            success = eventRepository.grantRole(giuseppeSessionId, vinoEventCod, "Giovanni Verdi", Role.ORGANIZER, listOf())
            Log.d("DB Prepopulate", "Role.ORGANIZER granted to Giovanni Verdi by Giuseppe Pagliaro in Event $vinoEventCod, success: $success")
            success = eventRepository.grantRole(giovanniSessionId, paneEventCod, "Giuseppe Pagliaro", Role.GUEST, listOf())
            Log.d("DB Prepopulate", "Role.GUEST granted to Giuseppe Pagliaro by Giovanni Verdi in Event $paneEventCod, success: $success")
            success = eventRepository.grantRole(giovanniSessionId, paneEventCod, "KIOSK", Role.GUEST, listOf())
            Log.d("DB Prepopulate", "Role.GUEST granted to KIOSK by Giovanni Verdi in Event $paneEventCod, success: $success")
            success = eventRepository.grantRole(giuseppeSessionId, vinoEventCod, "Malcom Smith", Role.ORGANIZER, listOf())
            Log.d("DB Prepopulate", "Role.ORGANIZER granted to Malcom Smith by Giuseppe Pagliaro in Event $vinoEventCod, success: $success")
            success = eventRepository.grantRole(giovanniSessionId, paneEventCod, "Malcom Smith", Role.ORGANIZER, listOf())
            Log.d("DB Prepopulate", "Role.ORGANIZER granted to Malcom Smith by Giovanni Verdi in Event $paneEventCod, success: $success")

            // Logout

            success = userRepository.logout(giuseppeSessionId)
            Log.d("DB Prepopulate", "Logout of Giuseppe, success: $success")

            success = userRepository.logout(giovanniSessionId)
            Log.d("DB Prepopulate", "Logout of Giovanni, success: $success")
        }
    }
}