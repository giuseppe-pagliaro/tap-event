package com.giuseppepagliaro.tapevent

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.giuseppepagliaro.tapevent.nfc.NfcView
import com.giuseppepagliaro.tapevent.nfc.getFromIntent
import com.google.android.material.bottomnavigation.BottomNavigationView

class EventActivity : AppCompatActivity() {
    private lateinit var fragment: Fragment

    override fun onCreate(savedInstanceState: Bundle?) {
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
                    fragment = DummyEventFragment() // TODO
                    loadFragment()
                    true
                }

                R.id.event_nav_cashpoints -> {
                    fragment = DummyItemSelectorFragmentWithCustomerCreation() // TODO
                    loadFragment()
                    true
                }

                R.id.event_nav_stands -> {
                    fragment = DummyItemSelectorFragmentNoCustomerCreation() // TODO
                    loadFragment()
                    true
                }

                else -> false
            }
        }

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
        // Rimpiazzo il Fragment contenuto nel fragment_container
        // con quello selezionato dall'utente.
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.event_fragment_container, fragment)
            .commit()
    }
}