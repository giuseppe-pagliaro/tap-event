package com.giuseppepagliaro.tapevent

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.giuseppepagliaro.tapevent.daos.CashPointDao
import com.giuseppepagliaro.tapevent.daos.CpManagesDao
import com.giuseppepagliaro.tapevent.daos.CustomerDao
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
import com.giuseppepagliaro.tapevent.repositories.CashPointRepository
import com.giuseppepagliaro.tapevent.repositories.EventsRepository
import com.giuseppepagliaro.tapevent.repositories.StandRepository
import com.giuseppepagliaro.tapevent.users.Session
import com.giuseppepagliaro.tapevent.users.SessionDao
import com.giuseppepagliaro.tapevent.users.User
import com.giuseppepagliaro.tapevent.users.UserDao
import com.giuseppepagliaro.tapevent.users.UserRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Date

@Database(
    entities = [
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

    version = 1,

    exportSchema = true
)
abstract class TapEventDatabase : RoomDatabase() {
    abstract fun internalUsers(): InternalUserDao
    abstract fun events(): EventDao
    abstract fun tickets(): TicketTypeDao
    abstract fun products(): ProductDao
    abstract fun cashPoints(): CashPointDao
    abstract fun stands(): StandDao
    abstract fun customers(): CustomerDao

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
                        CoroutineScope(Dispatchers.IO).launch {
                            populateDisplayData(context, getDatabase(context))
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
            val eventsRepository = EventsRepository(db)

            // Aggiungo User

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

            // Aggiungo Eventi

            success = eventsRepository.add(giuseppeSessionId, "Festa del Vino 2001", Date(995209200000))
            Log.d("DB Prepopulate", "Created \"Festa del Vino 2001\" Event: $success")
            success = eventsRepository.add(giovanniSessionId, "Festa del Pane 2077", Date(3400936200000))
            Log.d("DB Prepopulate", "Created \"Festa del Pane 2077\" Event: $success")
            val vinoEventCod = 1L
            val paneEventCod = 2L

            // Imposto Ruoli

            success = eventsRepository.grantRole(giuseppeSessionId, vinoEventCod, "KIOSK", Role.GUEST)
            Log.d("DB Prepopulate", "Role.GUEST granted to KIOSK by Giuseppe Pagliaro in Event $vinoEventCod, success: $success")
            success = eventsRepository.grantRole(giuseppeSessionId, vinoEventCod, "Giovanni Verdi", Role.ORGANIZER)
            Log.d("DB Prepopulate", "Role.ORGANIZER granted to Giovanni Verdi by Giuseppe Pagliaro in Event $vinoEventCod, success: $success")
            success = eventsRepository.grantRole(giovanniSessionId, paneEventCod, "Giuseppe Pagliaro", Role.GUEST)
            Log.d("DB Prepopulate", "Role.GUEST granted to Giuseppe Pagliaro by Giovanni Verdi in Event $paneEventCod, success: $success")
            success = eventsRepository.grantRole(giovanniSessionId, paneEventCod, "KIOSK", Role.GUEST)
            Log.d("DB Prepopulate", "Role.GUEST granted to KIOSK by Giovanni Verdi in Event $paneEventCod, success: $success")
            success = eventsRepository.grantRole(giuseppeSessionId, vinoEventCod, "Malcom Smith", Role.ORGANIZER)
            Log.d("DB Prepopulate", "Role.ORGANIZER granted to Malcom Smith by Giuseppe Pagliaro in Event $vinoEventCod, success: $success")
            success = eventsRepository.grantRole(giovanniSessionId, paneEventCod, "Malcom Smith", Role.ORGANIZER)
            Log.d("DB Prepopulate", "Role.ORGANIZER granted to Malcom Smith by Giovanni Verdi in Event $paneEventCod, success: $success")

            val vinoCashPointRepository = CashPointRepository(db, vinoEventCod)
            val vinoStandRepository = StandRepository(db, vinoEventCod)
            val paneCashPointRepository = CashPointRepository(db, paneEventCod)
            val paneStandRepository = StandRepository(db, paneEventCod)

            // Aggiungo Casse

            success = vinoCashPointRepository.add(giuseppeSessionId, "Cassa Entrata")
            Log.d("DB Prepopulate", "Added \"Cassa Entrata\" in Event $vinoEventCod by \"Giuseppe Pagliaro\": $success")
            success = vinoCashPointRepository.add(giuseppeSessionId, "Cassa Interna")
            Log.d("DB Prepopulate", "Added \"Cassa Interna\" in Event $vinoEventCod by \"Giuseppe Pagliaro\": $success")
            success = paneCashPointRepository.add(giovanniSessionId, "Cassa")
            Log.d("DB Prepopulate", "Added \"Cassa\" in Event $paneEventCod by \"Giovanni Verdi\": $success")

            // Aggiungo Tipi Ticket

            success = vinoCashPointRepository.addTicket(giuseppeSessionId, "Ticket Normale", 1.5F)
            Log.d("DB Prepopulate", "Added \"Ticket Normale\" in Event $vinoEventCod by \"Giuseppe Pagliaro\": $success")
            success = vinoCashPointRepository.addTicket(giuseppeSessionId, "Ticket Speciale", 4F)
            Log.d("DB Prepopulate", "Added \"Ticket Speciale\" in Event $vinoEventCod by \"Giuseppe Pagliaro\": $success")
            success = paneCashPointRepository.addTicket(giovanniSessionId, "Ticket Normale", 1.5F)
            Log.d("DB Prepopulate", "Added \"Ticket Normale\" in Event $paneEventCod by \"Giovanni Verdi\": $success")
            success = paneCashPointRepository.addTicket(giovanniSessionId, "Ticket Speciale", 4F)
            Log.d("DB Prepopulate", "Added \"Ticket Speciale\" in Event $paneEventCod by \"Giovanni Verdi\": $success")

            // Assegno i Ticket alle Casse

            success = vinoCashPointRepository.linkTicketToCashPoint(giuseppeSessionId, "Ticket Normale", "Cassa Entrata")
            Log.d("DB Prepopulate", "Linked \"Ticket Normale\" to \"Cassa Entrata\" in Event $vinoEventCod by \"Giuseppe Pagliaro\": $success")
            success = vinoCashPointRepository.linkTicketToCashPoint(giuseppeSessionId, "Ticket Speciale", "Cassa Entrata")
            Log.d("DB Prepopulate", "Linked \"Ticket Speciale\" to \"Cassa Entrata\" in Event $vinoEventCod by \"Giuseppe Pagliaro\": $success")
            success = vinoCashPointRepository.linkTicketToCashPoint(giuseppeSessionId, "Ticket Normale", "Cassa Interna")
            Log.d("DB Prepopulate", "Linked \"Ticket Normale\" to \"Cassa Interna\" in Event $vinoEventCod by \"Giuseppe Pagliaro\": $success")
            success = paneCashPointRepository.linkTicketToCashPoint(giovanniSessionId, "Ticket Normale", "Cassa")
            Log.d("DB Prepopulate", "Linked \"Ticket Normale\" to \"Cassa\" in Event $paneEventCod by \"Giovanni Verdi\": $success")
            success = paneCashPointRepository.linkTicketToCashPoint(giovanniSessionId, "Ticket Speciale", "Cassa")
            Log.d("DB Prepopulate", "Linked \"Ticket Speciale\" to \"Cassa\" in Event $paneEventCod by \"Giovanni Verdi\": $success")

            // Aggiungo Stand

            success = vinoStandRepository.add(giuseppeSessionId, "Stand Vino Rosso")
            Log.d("DB Prepopulate", "Added \"Stand Vino Rosso\" in Event $vinoEventCod by \"Giuseppe Pagliaro\": $success")
            success = vinoStandRepository.add(giuseppeSessionId, "Stand Vino Bianco")
            Log.d("DB Prepopulate", "Added \"Stand Vino Bianco\" in Event $vinoEventCod by \"Giuseppe Pagliaro\": $success")
            success = vinoStandRepository.add(giuseppeSessionId, "Stand Food")
            Log.d("DB Prepopulate", "Added \"Stand Food\" in Event $vinoEventCod by \"Giuseppe Pagliaro\": $success")
            success = paneStandRepository.add(giovanniSessionId, "Stand Pane")
            Log.d("DB Prepopulate", "Added \"Stand Pane\" in Event $paneEventCod by \"Giovanni Verdi\": $success")
            success = paneStandRepository.add(giovanniSessionId, "Stand Bevande")
            Log.d("DB Prepopulate", "Added \"Stand Bevande\" in Event $paneEventCod by \"Giovanni Verdi\": $success")

            // Aggiungo Prodotti

            success = vinoStandRepository.addProduct(giuseppeSessionId, "Bicchiere di Rosso del Savuto", Uri.parse(Product.DEFAULT_THUMBNAIL_URL)) // TODO thumbnail
            Log.d("DB Prepopulate", "Added \"Bicchiere di Rosso del Savuto\" in Event $vinoEventCod by \"Giuseppe Pagliaro\": $success")
            success = vinoStandRepository.addProduct(giuseppeSessionId, "Bicchiere di Rosso di Cirò", Uri.parse(Product.DEFAULT_THUMBNAIL_URL)) // TODO thumbnail
            Log.d("DB Prepopulate", "Added \"Bicchiere di Rosso di Cirò\" in Event $vinoEventCod by \"Giuseppe Pagliaro\": $success")
            success = vinoStandRepository.addProduct(giuseppeSessionId, "Bicchiere di Bianco del Savuto", Uri.parse(Product.DEFAULT_THUMBNAIL_URL)) // TODO thumbnail
            Log.d("DB Prepopulate", "Added \"Bicchiere di Bianco del Savuto\" in Event $vinoEventCod by \"Giuseppe Pagliaro\": $success")
            success = vinoStandRepository.addProduct(giuseppeSessionId, "Bicchiere di Bianco di Cirò", Uri.parse(Product.DEFAULT_THUMBNAIL_URL)) // TODO thumbnail
            Log.d("DB Prepopulate", "Added \"Bicchiere di Bianco di Cirò\" in Event $vinoEventCod by \"Giuseppe Pagliaro\": $success")
            success = vinoStandRepository.addProduct(giuseppeSessionId, "Caraffa (1L) di Rosso di Cirò", Uri.parse(Product.DEFAULT_THUMBNAIL_URL)) // TODO thumbnail
            Log.d("DB Prepopulate", "Added \"Caraffa (1L) di Rosso di Cirò\" in Event $vinoEventCod by \"Giuseppe Pagliaro\": $success")
            success = vinoStandRepository.addProduct(giuseppeSessionId, "Caraffa (1L) di Bianco di Cirò", Uri.parse(Product.DEFAULT_THUMBNAIL_URL)) // TODO thumbnail
            Log.d("DB Prepopulate", "Added \"Caraffa (1L) di Bianco di Cirò\" in Event $vinoEventCod by \"Giuseppe Pagliaro\": $success")
            success = vinoStandRepository.addProduct(giuseppeSessionId, "Bicchiere di Rosato", Uri.parse(Product.DEFAULT_THUMBNAIL_URL)) // TODO thumbnail
            Log.d("DB Prepopulate", "Added \"Bicchiere di Rosato\" in Event $vinoEventCod by \"Giuseppe Pagliaro\": $success")
            success = vinoStandRepository.addProduct(giuseppeSessionId, "Caraffa (1L) di Rosato", Uri.parse(Product.DEFAULT_THUMBNAIL_URL)) // TODO thumbnail
            Log.d("DB Prepopulate", "Added \"Caraffa (1L) di Rosato\" in Event $vinoEventCod by \"Giuseppe Pagliaro\": $success")
            success = vinoStandRepository.addProduct(giuseppeSessionId, "Bicchiere di Champagne", Uri.parse(Product.DEFAULT_THUMBNAIL_URL)) // TODO thumbnail
            Log.d("DB Prepopulate", "Added \"Bicchiere di Champagne\" in Event $vinoEventCod by \"Giuseppe Pagliaro\": $success")
            success = vinoStandRepository.addProduct(giuseppeSessionId, "Bottiglia di Champagne", Uri.parse(Product.DEFAULT_THUMBNAIL_URL)) // TODO thumbnail
            Log.d("DB Prepopulate", "Added \"Bottiglia di Champagne\" in Event $vinoEventCod by \"Giuseppe Pagliaro\": $success")
            success = vinoStandRepository.addProduct(giuseppeSessionId, "Menu: Panino + Bicchiere di Vino", Uri.parse(Product.DEFAULT_THUMBNAIL_URL)) // TODO thumbnail
            Log.d("DB Prepopulate", "Added \"Menu: Panino + Bicchiere di Vino\" in Event $vinoEventCod by \"Giuseppe Pagliaro\": $success")
            success = vinoStandRepository.addProduct(giuseppeSessionId, "Pasta", Uri.parse(Product.DEFAULT_THUMBNAIL_URL)) // TODO thumbnail
            Log.d("DB Prepopulate", "Added \"Pasta\" in Event $vinoEventCod by \"Giuseppe Pagliaro\": $success")
            success = vinoStandRepository.addProduct(giuseppeSessionId, "Panino", Uri.parse(Product.DEFAULT_THUMBNAIL_URL)) // TODO thumbnail
            Log.d("DB Prepopulate", "Added \"Panino\" in Event $vinoEventCod by \"Giuseppe Pagliaro\": $success")
            success = vinoStandRepository.addProduct(giuseppeSessionId, "Arancino", Uri.parse(Product.DEFAULT_THUMBNAIL_URL)) // TODO thumbnail
            Log.d("DB Prepopulate", "Added \"Arancino\" in Event $vinoEventCod by \"Giuseppe Pagliaro\": $success")
            success = vinoStandRepository.addProduct(giuseppeSessionId, "Spiedini", Uri.parse(Product.DEFAULT_THUMBNAIL_URL)) // TODO thumbnail
            Log.d("DB Prepopulate", "Added \"Spiedini\" in Event $vinoEventCod by \"Giuseppe Pagliaro\": $success")
            success = vinoStandRepository.addProduct(giuseppeSessionId, "Menu: Rosticceria x 3 Persone", Uri.parse(Product.DEFAULT_THUMBNAIL_URL)) // TODO thumbnail
            Log.d("DB Prepopulate", "Added \"Menu: Rosticceria x 3 Persone\" in Event $vinoEventCod by \"Giuseppe Pagliaro\": $success")
            success = vinoStandRepository.addProduct(giuseppeSessionId, "Cartoccio di Cardarroste", Uri.parse(Product.DEFAULT_THUMBNAIL_URL)) // TODO thumbnail
            Log.d("DB Prepopulate", "Added \"Cartoccio di Cardarroste\" in Event $vinoEventCod by \"Giuseppe Pagliaro\": $success")
            success = vinoStandRepository.addProduct(giuseppeSessionId, "Pitta con Trippa", Uri.parse(Product.DEFAULT_THUMBNAIL_URL)) // TODO thumbnail
            Log.d("DB Prepopulate", "Added \"Pitta con Trippa\" in Event $vinoEventCod by \"Giuseppe Pagliaro\": $success")
            success = paneStandRepository.addProduct(giovanniSessionId, "Pane e Olio", Uri.parse(Product.DEFAULT_THUMBNAIL_URL)) // TODO thumbnail
            Log.d("DB Prepopulate", "Added \"Pane e Olio\" in Event $paneEventCod by \"Giovanni Verdi\": $success")
            success = paneStandRepository.addProduct(giovanniSessionId, "Degustazione di Pane e Crostini", Uri.parse(Product.DEFAULT_THUMBNAIL_URL)) // TODO thumbnail
            Log.d("DB Prepopulate", "Added \"Degustazione di Pane e Crostini\" in Event $paneEventCod by \"Giovanni Verdi\": $success")
            success = paneStandRepository.addProduct(giovanniSessionId, "Focaccia", Uri.parse(Product.DEFAULT_THUMBNAIL_URL)) // TODO thumbnail
            Log.d("DB Prepopulate", "Added \"Focaccia\" in Event $paneEventCod by \"Giovanni Verdi\": $success")
            success = paneStandRepository.addProduct(giovanniSessionId, "Pitta", Uri.parse(Product.DEFAULT_THUMBNAIL_URL)) // TODO thumbnail
            Log.d("DB Prepopulate", "Added \"Pitta\" in Event $paneEventCod by \"Giovanni Verdi\": $success")
            success = paneStandRepository.addProduct(giovanniSessionId, "Pane Integrale", Uri.parse(Product.DEFAULT_THUMBNAIL_URL)) // TODO thumbnail
            Log.d("DB Prepopulate", "Added \"Pane Integrale\" in Event $paneEventCod by \"Giovanni Verdi\": $success")
            success = paneStandRepository.addProduct(giovanniSessionId, "Birra", Uri.parse(Product.DEFAULT_THUMBNAIL_URL)) // TODO thumbnail
            Log.d("DB Prepopulate", "Added \"Birra\" in Event $paneEventCod by \"Giovanni Verdi\": $success")
            success = paneStandRepository.addProduct(giovanniSessionId, "Coca Cola", Uri.parse(Product.DEFAULT_THUMBNAIL_URL)) // TODO thumbnail
            Log.d("DB Prepopulate", "Added \"Coca Cola\" in Event $paneEventCod by \"Giovanni Verdi\": $success")
            success = paneStandRepository.addProduct(giovanniSessionId, "Acqua", Uri.parse(Product.DEFAULT_THUMBNAIL_URL)) // TODO thumbnail
            Log.d("DB Prepopulate", "Added \"Acqua\" in Event $paneEventCod by \"Giovanni Verdi\": $success")

            // Associo i Prodotti agli Stand

            success = vinoStandRepository.setPriceInStand(giuseppeSessionId, "Bicchiere di Rosso del Savuto", "Stand Vino Rosso", "Ticket Normale", 2)
            Log.d("DB Prepopulate", "Linked \"Bicchiere di Rosso del Savuto\" to \"Stand Vino Rosso\", sold for 2 \"Ticket Normale\" in Event $vinoEventCod by \"Giuseppe Pagliaro\": $success")
            success = vinoStandRepository.setPriceInStand(giuseppeSessionId, "Bicchiere di Rosso di Cirò", "Stand Vino Rosso", "Ticket Normale", 1)
            Log.d("DB Prepopulate", "Linked \"Bicchiere di Rosso di Cirò\" to \"Stand Vino Rosso\", sold for 1 \"Ticket Normale\" in Event $vinoEventCod by \"Giuseppe Pagliaro\": $success")
            success = vinoStandRepository.setPriceInStand(giuseppeSessionId, "Bicchiere di Bianco del Savuto", "Stand Vino Bianco", "Ticket Normale", 2)
            Log.d("DB Prepopulate", "Linked \"Bicchiere di Bianco del Savuto\" to \"Stand Vino Bianco\", sold for 2 \"Ticket Normale\" in Event $vinoEventCod by \"Giuseppe Pagliaro\": $success")
            success = vinoStandRepository.setPriceInStand(giuseppeSessionId, "Bicchiere di Bianco di Cirò", "Stand Vino Bianco", "Ticket Normale", 1)
            Log.d("DB Prepopulate", "Linked \"Bicchiere di Bianco di Cirò\" to \"Stand Vino Bianco\", sold for 1 \"Ticket Normale\" in Event $vinoEventCod by \"Giuseppe Pagliaro\": $success")
            success = vinoStandRepository.setPriceInStand(giuseppeSessionId, "Caraffa (1L) di Rosso di Cirò", "Stand Vino Rosso", "Ticket Normale", 8)
            Log.d("DB Prepopulate", "Linked \"Caraffa (1L) di Rosso di Cirò\" to \"Stand Vino Bianco\", sold for 8 \"Ticket Normale\" in Event $vinoEventCod by \"Giuseppe Pagliaro\": $success")
            success = vinoStandRepository.setPriceInStand(giuseppeSessionId, "Caraffa (1L) di Bianco di Cirò", "Stand Vino Bianco", "Ticket Normale", 8)
            Log.d("DB Prepopulate", "Linked \"Caraffa (1L) di Bianco di Cirò\" to \"Stand Vino Rosso\", sold for 8 \"Ticket Normale\" in Event $vinoEventCod by \"Giuseppe Pagliaro\": $success")
            success = vinoStandRepository.setPriceInStand(giuseppeSessionId, "Bicchiere di Rosato", "Stand Vino Rosso", "Ticket Normale", 2)
            Log.d("DB Prepopulate", "Linked \"Bicchiere di Rosato\" to \"Stand Vino Rosso\", sold for 2 \"Ticket Normale\" in Event $vinoEventCod by \"Giuseppe Pagliaro\": $success")
            success = vinoStandRepository.setPriceInStand(giuseppeSessionId, "Caraffa (1L) di Rosato", "Stand Vino Rosso", "Ticket Speciale", 2)
            Log.d("DB Prepopulate", "Linked \"Caraffa (1L) di Rosato\" to \"Stand Vino Rosso\", sold for 2 \"Ticket Speciale\" in Event $vinoEventCod by \"Giuseppe Pagliaro\": $success")
            success = vinoStandRepository.setPriceInStand(giuseppeSessionId, "Bicchiere di Champagne", "Stand Vino Bianco", "Ticket Speciale", 1)
            Log.d("DB Prepopulate", "Linked \"Bicchiere di Champagne\", to \"Stand Vino Bianco\", sold for 1 \"Ticket Speciale\" in Event $vinoEventCod by \"Giuseppe Pagliaro\": $success")
            success = vinoStandRepository.setPriceInStand(giuseppeSessionId, "Bottiglia di Champagne", "Stand Vino Bianco", "Ticket Normale", 2)
            Log.d("DB Prepopulate", "Linked \"Bottiglia di Champagne\" to \"Stand Vino Bianco\", sold for 2 \"Ticket Normale\" in Event $vinoEventCod by \"Giuseppe Pagliaro\": $success")
            success = vinoStandRepository.setPriceInStand(giuseppeSessionId, "Menu: Panino + Bicchiere di Vino", "Stand Food", "Ticket Speciale", 2)
            Log.d("DB Prepopulate", "Linked \"Menu: Panino + Bicchiere di Vino\" to \"Stand Food\", sold for 2 \"Ticket Speciale\" in Event $vinoEventCod by \"Giuseppe Pagliaro\": $success")
            success = vinoStandRepository.setPriceInStand(giuseppeSessionId, "Pasta", "Stand Food", "Ticket Speciale", 1)
            Log.d("DB Prepopulate", "Linked \"Pasta\" to \"Stand Food\", sold for 1 \"Ticket Speciale\" in Event $vinoEventCod by \"Giuseppe Pagliaro\": $success")
            success = vinoStandRepository.setPriceInStand(giuseppeSessionId, "Panino", "Stand Food", "Ticket Normale", 3)
            Log.d("DB Prepopulate", "Linked \"Panino\" to \"Stand Food\", sold for 3 \"Ticket Normale\" in Event $vinoEventCod by \"Giuseppe Pagliaro\": $success")
            success = vinoStandRepository.setPriceInStand(giuseppeSessionId, "Arancino", "Stand Food", "Ticket Normale", 2)
            Log.d("DB Prepopulate", "Linked \"Arancino\" to \"Stand Food\", sold for 2 \"Ticket Normale\" in Event $vinoEventCod by \"Giuseppe Pagliaro\": $success")
            success = vinoStandRepository.setPriceInStand(giuseppeSessionId, "Spiedini", "Stand Food", "Ticket Normale", 2)
            Log.d("DB Prepopulate", "Linked \"Spiedini\" to \"Stand Food\", sold for 2 \"Ticket Normale\" in Event $vinoEventCod by \"Giuseppe Pagliaro\": $success")
            success = vinoStandRepository.setPriceInStand(giuseppeSessionId, "Menu: Rosticceria x 3 Persone", "Stand Food", "Ticket Speciale", 1)
            Log.d("DB Prepopulate", "Linked \"Menu: Rosticceria x 3 Persone\" to \"Stand Food\", sold for 1 \"Ticket Speciale\" in Event $vinoEventCod by \"Giuseppe Pagliaro\": $success")
            success = vinoStandRepository.setPriceInStand(giuseppeSessionId, "Cartoccio di Cardarroste", "Stand Food", "Ticket Speciale", 2)
            Log.d("DB Prepopulate", "Linked \"Cartoccio di Cardarroste\" to \"Stand Food\", sold for 2 \"Ticket Speciale\" in Event $vinoEventCod by \"Giuseppe Pagliaro\": $success")
            success = vinoStandRepository.setPriceInStand(giuseppeSessionId, "Pitta con Trippa", "Stand Food", "Ticket Normale", 4)
            Log.d("DB Prepopulate", "Linked \"Pitta con Trippa\" to \"Stand Food\", sold for 4 \"Ticket Normale\" in Event $vinoEventCod by \"Giuseppe Pagliaro\": $success")
            success = paneStandRepository.setPriceInStand(giovanniSessionId, "Pane e Olio", "Stand Pane", "Ticket Normale", 1)
            Log.d("DB Prepopulate", "Linked \"Pane e Olio\" to \"Stand Pane\", sold for 2 \"Ticket Normale\" in Event $paneEventCod by \"Giovanni Verdi\": $success")
            success = paneStandRepository.setPriceInStand(giovanniSessionId, "Degustazione di Pane e Crostini", "Stand Pane", "Ticket Normale", 1)
            Log.d("DB Prepopulate", "Linked \"Degustazione di Pane e Crostini\" to \"Stand Pane\", sold for 2 \"Ticket Normale\" in Event $paneEventCod by \"Giovanni Verdi\": $success")
            success = paneStandRepository.setPriceInStand(giovanniSessionId, "Focaccia", "Stand Pane", "Ticket Normale", 1)
            Log.d("DB Prepopulate", "Linked \"Focaccia\" to \"Stand Pane\", sold for 1 \"Ticket Normale\" in Event $paneEventCod by \"Giovanni Verdi\": $success")
            success = paneStandRepository.setPriceInStand(giovanniSessionId, "Pitta", "Stand Pane", "Ticket Normale", 2)
            Log.d("DB Prepopulate", "Linked \"Pitta\" to \"Stand Pane\", sold for 2 \"Ticket Normale\" in Event $paneEventCod by \"Giovanni Verdi\": $success")
            success = paneStandRepository.setPriceInStand(giovanniSessionId, "Pane Integrale", "Stand Pane", "Ticket Normale", 2)
            Log.d("DB Prepopulate", "Linked \"Pane Integrale\" to \"Stand Pane\", sold for 2 \"Ticket Normale\" in Event $paneEventCod by \"Giovanni Verdi\": $success")
            success = paneStandRepository.setPriceInStand(giovanniSessionId, "Birra", "Stand Bevande", "Ticket Normale", 2)
            Log.d("DB Prepopulate", "Linked \"Birra\" to \"Stand Bevande\", sold for 2 \"Ticket Normale\" in Event $paneEventCod by \"Giovanni Verdi\": $success")
            success = paneStandRepository.setPriceInStand(giovanniSessionId, "Coca Cola", "Stand Bevande", "Ticket Normale", 1)
            Log.d("DB Prepopulate", "Linked \"Coca Cola\" to \"Stand Bevande\", sold for 1 \"Ticket Normale\" in Event $paneEventCod by \"Giovanni Verdi\": $success")
            success = paneStandRepository.setPriceInStand(giovanniSessionId, "Acqua", "Stand Bevande", "Ticket Normale", 1)
            Log.d("DB Prepopulate", "Linked \"Acqua\" to \"Stand Bevande\", sold for 1 \"Ticket Normale\" in Event $paneEventCod by \"Giovanni Verdi\": $success")

            // Imposto Ruoli di Cashier e Stand Keeper

            success = eventsRepository.grantRole(giuseppeSessionId, vinoEventCod, "Malcom Smith", Role.CASHIER)
            Log.d("DB Prepopulate", "Role.CASHIER granted to Malcom Smith by Giuseppe Pagliaro in Event $vinoEventCod, success: $success")
            success = eventsRepository.grantRole(giuseppeSessionId, vinoEventCod, "Malcom Smith", Role.STAND_KEEPER, listOf("Stand Vino Rosso", "Stand Vino Bianco"))
            Log.d("DB Prepopulate", "Role.STAND_KEEPER(Stand Vino Rosso, Stand Vino Bianco) granted to Malcom Smith by Giuseppe Pagliaro in Event $vinoEventCod, success: $success")
            success = eventsRepository.grantRole(giuseppeSessionId, vinoEventCod, "Alberto Toscano", Role.STAND_KEEPER, listOf("Stand Food"))
            Log.d("DB Prepopulate", "Role.STAND_KEEPER(Stand Food) granted to Alberto Toscano by Giuseppe Pagliaro in Event $vinoEventCod, success: $success")
            success = eventsRepository.grantRole(giovanniSessionId, paneEventCod, "Malcom Smith", Role.STAND_KEEPER, listOf("Stand Bevande"))
            Log.d("DB Prepopulate", "Role.STAND_KEEPER(Stand Bevande) granted to Malcom Smith by Giovanni Verdi in Event $paneEventCod, success: $success")
            success = eventsRepository.grantRole(giovanniSessionId, paneEventCod, "Malcom Smith", Role.CASHIER, listOf("Cassa"))
            Log.d("DB Prepopulate", "Role.CASHIER(Cassa) granted to Malcom Smith by Giovanni Verdi in Event $paneEventCod, success: $success")
            success = eventsRepository.grantRole(giovanniSessionId, paneEventCod, "Alberto Toscano", Role.STAND_KEEPER, listOf("Stand Pane"))
            Log.d("DB Prepopulate", "Role.STAND_KEEPER(Stand Pane) granted to Alberto Toscano by Giovanni Verdi in Event $paneEventCod, success: $success")

            // Logout

            success = userRepository.logout(giuseppeSessionId)
            Log.d("DB Prepopulate", "Logout of Giuseppe, success: $success")
            success = userRepository.logout(giovanniSessionId)
            Log.d("DB Prepopulate", "Logout of Giovanni, success: $success")
        }
    }
}