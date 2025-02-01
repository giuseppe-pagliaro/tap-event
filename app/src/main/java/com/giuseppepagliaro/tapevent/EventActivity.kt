package com.giuseppepagliaro.tapevent

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.giuseppepagliaro.tapevent.viewmodels.DummyItemSelectorViewModel

class EventActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_event)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val fragment = ItemSelectorFragment {
            DummyItemSelectorViewModel::class.java
        }
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.event_fragment_container, fragment)
            .commit()
    }
}