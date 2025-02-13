package com.giuseppepagliaro.tapevent

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.giuseppepagliaro.tapevent.models.Role
import com.giuseppepagliaro.tapevent.nfc.NfcView
import com.giuseppepagliaro.tapevent.nfc.getFromIntent
import com.giuseppepagliaro.tapevent.repositories.EventsRepository
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch

class EventActivity : AppCompatActivity() {
    private lateinit var sessionId: String
    private var eventCod: Long = -1

    private lateinit var fragment: Fragment

    override fun onCreate(savedInstanceState: Bundle?) {
        sessionId = intent.getStringExtra("session_id") ?: run {
            MainActivity.onSessionIdInvalidated(this)
            return
        }
        eventCod = intent.getLongExtra("event_cod", -1)
        if (eventCod == -1L)
            throw IllegalArgumentException("Event cod needed to run EventActivity.")

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_event)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Configuro Navbar.
        val navbar: BottomNavigationView = findViewById(R.id.event_navbar)
        navbar.setOnItemSelectedListener { item ->
            when(item.itemId) {
                R.id.event_nav_event -> {
                    fragment = EventFragmentImpl()
                    loadFragment()
                    true
                }

                R.id.event_nav_cashpoints -> {
                    fragment = TicketItemSelectorFragment()
                    loadFragment()
                    true
                }

                R.id.event_nav_stands -> {
                    fragment = ProductsItemSelectorFragment()
                    loadFragment()
                    true
                }

                else -> false
            }
        }

        var role: Role = Role.GUEST
        lifecycleScope.launch {
            val eventRepository = EventsRepository(TapEventDatabase.getDatabase(this@EventActivity))
            role = eventRepository.getUserRole(sessionId, eventCod) ?: run {
                MainActivity.onSessionIdInvalidated(this@EventActivity)
                return@launch
            }
        }.invokeOnCompletion { lifecycleScope.launch {
            when(role) {
                Role.GUEST -> navbar.visibility = View.GONE

                Role.CASHIER -> navbar.inflateMenu(R.menu.event_bottom_navigation_cashier)

                Role.STAND_KEEPER -> navbar.inflateMenu(R.menu.event_bottom_navigation_standkeeper)

                else -> {}
            }
        } }

        // Seleziono il fragment di default.
        navbar.selectedItemId = R.id.event_nav_event
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        // Gestisci nfc intent
        if (getFromIntent(intent) != null) {
            (fragment as NfcView).handleNfcIntent(intent)
        }
    }

    private fun loadFragment() {
        // Inserisco gli argomenti nel fragment.
        fragment.arguments = Bundle().apply {
            putString("session_id", sessionId)
            putLong("event_cod", eventCod)
        }
        // Rimpiazzo il Fragment contenuto nel fragment_container
        // con quello selezionato dall'utente.
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.event_fragment_container, fragment)
            .commit()
    }
}