package com.giuseppepagliaro.tapevent

import android.os.Bundle
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.AutoTransition
import androidx.transition.TransitionManager
import com.giuseppepagliaro.tapevent.adapters.ItemDisplayableAdapter
import com.giuseppepagliaro.tapevent.viewmodels.ListActivityViewModel

abstract class ListActivity : AppCompatActivity() {
    protected abstract fun getListActivityViewModelFactory(): ListActivityViewModel.Factory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_list)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val viewModel = ViewModelProvider(
            this,
            getListActivityViewModelFactory()
        )[ListActivityViewModel::class.java]

        val listsTransition = AutoTransition().apply {
            duration = 85 // milliseconds
        }

        val viewRoot: ViewGroup = findViewById(R.id.main)
        val tvTitle: TextView = findViewById(R.id.tv_items_title)
        val rwItems: RecyclerView = findViewById(R.id.rw_items)
        val btnBack: ImageButton = findViewById(R.id.btn_items_back)

        // Configura Titolo
        tvTitle.text = viewModel.itemsName

        // Configura Items Recycle View
        rwItems.layoutManager = LinearLayoutManager(this)
        rwItems.adapter = ItemDisplayableAdapter(
            this,
            listOf()
        )
        viewModel.items.observe(this) { items ->
            if (items.isNullOrEmpty()) return@observe

            TransitionManager.beginDelayedTransition(viewRoot, listsTransition)

            // Se non ci sono oggetti selezionati,
            // la recycler view non deve essere mostrata.
            (rwItems.adapter as ItemDisplayableAdapter).updateItems(items)

            viewRoot.requestLayout()
        }

        // Configura Button Back
        btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }
}