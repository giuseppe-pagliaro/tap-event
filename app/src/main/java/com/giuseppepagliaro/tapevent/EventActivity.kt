package com.giuseppepagliaro.tapevent

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.giuseppepagliaro.tapevent.nfc.getFromIntent
import com.giuseppepagliaro.tapevent.viewmodels.DummyItemSelectorViewModel

class EventActivity : AppCompatActivity() {
    private var currentItemSelectorFragment: ItemSelectorFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_event)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        currentItemSelectorFragment = DummyItemSelectorFragmentWithCustomerCreation()

        supportFragmentManager
            .beginTransaction()
            .replace(
                R.id.event_fragment_container,
                currentItemSelectorFragment!!
            )
            .commit()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        // Gestisci nfc intent
        if (getFromIntent(intent) != null) {
            currentItemSelectorFragment?.handleNfcIntent(intent)
        }
    }
}