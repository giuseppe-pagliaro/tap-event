package com.giuseppepagliaro.tapevent

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.giuseppepagliaro.tapevent.daos.BoughtWithDao
import com.giuseppepagliaro.tapevent.daos.CpManagesDao
import com.giuseppepagliaro.tapevent.daos.InternalUserDao
import com.giuseppepagliaro.tapevent.daos.OwnsDao
import com.giuseppepagliaro.tapevent.daos.PSellsDao
import com.giuseppepagliaro.tapevent.daos.ParticipatesDao
import com.giuseppepagliaro.tapevent.daos.SManagesDao
import com.giuseppepagliaro.tapevent.daos.TSellsDao
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

            var created = userRepository.add("owner", "opass")
            Log.d("DB Prepopulate", "Created owner: $created")

            created = userRepository.add("admin", "apass")
            Log.d("DB Prepopulate", "Created admin: $created")

            created = userRepository.add("cashier", "cpass")
            Log.d("DB Prepopulate", "Created cashier: $created")

            created = userRepository.add("stander", "spass")
            Log.d("DB Prepopulate", "Created stander: $created")

            created = userRepository.add("guest", "gpass")
            Log.d("DB Prepopulate", "Created guest: $created")
        }
    }
}