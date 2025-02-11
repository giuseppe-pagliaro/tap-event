package com.giuseppepagliaro.tapevent

import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.AutoTransition
import androidx.transition.TransitionManager
import com.bumptech.glide.Glide
import com.giuseppepagliaro.tapevent.adapters.ItemEventAdapter
import com.giuseppepagliaro.tapevent.adapters.NoItemsAdapter
import com.giuseppepagliaro.tapevent.viewmodels.HomeActivityViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch

abstract class HomeActivity : AppCompatActivity() {
    protected abstract suspend fun getViewModelFactory(): HomeActivityViewModel.Factory
    protected abstract fun putSessionIdIntoIntent(intent: Intent)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val activity = this
        lifecycleScope.launch {
            val viewModel = ViewModelProvider(
                activity,
                getViewModelFactory()
            )[HomeActivityViewModel::class.java]

            val listsTransition = AutoTransition().apply {
                duration = 85 // milliseconds
            }

            val viewRoot: ViewGroup = findViewById(R.id.main)
            val cvProfile: CardView = findViewById(R.id.cv_profile)
            val ivProfile: ImageView = findViewById(R.id.iv_profile)
            val tvWelcome: TextView = findViewById(R.id.tv_welcome)
            val rwEvents: RecyclerView = findViewById(R.id.rw_events)

            // Configuro l'evento di Logout
            viewModel.logoutEvent.observe(activity) { success ->
                if (!success) {
                    Toast.makeText(
                        activity,
                        getString(R.string.home_profile_logout_error),
                        Toast.LENGTH_SHORT
                    ).show()

                    return@observe
                }

                val intent = Intent(activity, MainActivity::class.java)

                // Le flag servono a resettare il Navigation Stack,
                // per evitare che l'utente possa tornare indietro.
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                intent.putExtra("was_session_invalidated", true)
                startActivity(intent)

                // Chiudo l'Activity corrente.
                finish()
            }

            // Configuro il Logout Popup
            cvProfile.setOnClickListener {
                val responses = arrayOf(
                    getString(R.string.home_profile_logout_yes),
                    getString(R.string.home_profile_logout_no)
                )
                MaterialAlertDialogBuilder(activity)
                    .setTitle(getString(R.string.home_profile_logout))
                    .setItems(responses) { _, which ->
                        if (which == 0/* Yes */)
                            viewModel.logout()
                    }
                    .show()
            }

            // Osservo i LiveData

            viewModel.profilePic.observe(activity) { uri ->
                if (uri == null) return@observe

                Glide
                    .with(activity)
                    .load(uri)
                    .into(ivProfile)
            }

            viewModel.username.observe(activity) { username ->
                if (username.isNullOrEmpty()) return@observe

                tvWelcome.text = getString(R.string.home_welcome_message, username)
            }

            rwEvents.layoutManager = LinearLayoutManager(activity)
            val eventsAdapter = ItemEventAdapter(
                activity,
                listOf(),
                viewModel.getRoleColor,
                activity::openEventActivity
            )
            val noEventsAdapter = NoItemsAdapter(activity, getString(R.string.event_nav_event_title))
            rwEvents.adapter = noEventsAdapter

            viewModel.events.observe(activity) { events ->
                if (events == null) return@observe

                if (events.isEmpty()) {
                    TransitionManager.beginDelayedTransition(viewRoot, listsTransition)

                    rwEvents.adapter = noEventsAdapter

                    viewRoot.requestLayout()
                } else {
                    TransitionManager.beginDelayedTransition(viewRoot, listsTransition)

                    if (rwEvents.adapter == noEventsAdapter)
                        rwEvents.adapter = eventsAdapter

                    (rwEvents.adapter as ItemEventAdapter).updateItems(events)

                    viewRoot.requestLayout()
                }
            }
        }
    }

    private fun openEventActivity(eventCod: Long) {
        val intent = Intent(this, EventActivity::class.java)
        intent.putExtra("event_cod", eventCod)
        putSessionIdIntoIntent(intent)

        startActivity(intent)
    }
}