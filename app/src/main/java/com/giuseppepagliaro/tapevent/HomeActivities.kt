package com.giuseppepagliaro.tapevent

import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.util.Log
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.giuseppepagliaro.tapevent.models.EventInfo
import com.giuseppepagliaro.tapevent.models.Role
import com.giuseppepagliaro.tapevent.repositories.EventRepository
import com.giuseppepagliaro.tapevent.users.UserRepository
import com.giuseppepagliaro.tapevent.viewmodels.HomeActivityViewModel
import java.util.Date

class HomeActivityImpl : HomeActivity() {
    private lateinit var sessionId: String

    override fun getViewModelFactory(): HomeActivityViewModel.Factory {
        val userRepository = UserRepository() // TODO init
        val eventRepository = EventRepository() // TODO init

        sessionId = intent.getStringExtra("session_id") ?: run {
            MainActivity.onSessionIdInvalidated(this)

            // Ritorna un'istanza banale della Factory, perché tanto la view non verrà
            // mai mostrata se si raggiunge questo punto.
            return DummyHomeActivity.getDummyFactory(false)
        }
        val userInfo = userRepository.getUserInfo(sessionId) ?: run {
            MainActivity.onSessionIdInvalidated(this)
            return DummyHomeActivity.getDummyFactory(false)
        }
        val events = eventRepository.getAll(sessionId) ?: run {
            MainActivity.onSessionIdInvalidated(this)
            return DummyHomeActivity.getDummyFactory(false)
        }

        val username = MediatorLiveData<String>().apply {
            addSource(userInfo) { user ->
                value = user.username
            }
        }
        val profilePicture = MediatorLiveData<Uri>().apply {
            addSource(userInfo) { user ->
                value = user.profilePicture
            }
        }

        return HomeActivityViewModel.Factory(
            username,
            profilePicture,
            events,
            {
                if (isDarkModeEnabled()) R.color.eerie_black
                else R.color.whitesmoke
            },
            { userRepository.logout(sessionId) }
        )
    }

    override fun putSessionIdIntoIntent(intent: Intent) {
        intent.putExtra("session_id", sessionId)
    }

    private fun isDarkModeEnabled(): Boolean {
        return (resources.configuration.uiMode and
                Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
    }
}

class DummyHomeActivity : HomeActivity() {
    companion object {
        private const val LOG_TAG = "DummyHomeActivity"

        fun getDummyFactory(isDarkModeEnabled: Boolean): HomeActivityViewModel.Factory {
            return HomeActivityViewModel.Factory(
                MutableLiveData("User"),
                MutableLiveData(
                    Uri.parse(
                        "android.resource://com.giuseppepagliaro.tapevent/drawable/profile"
                    )
                ),
                MutableLiveData(
                    listOf(
                        EventInfo(0, "Event 1", Date(), Role.GUEST),
                        EventInfo(1, "Event 2", Date(), Role.GUEST),
                        EventInfo(2, "Event 3", Date(), Role.GUEST),
                        EventInfo(3, "Event 4", Date(), Role.GUEST),
                        EventInfo(4, "Event 5", Date(), Role.GUEST),
                        EventInfo(5, "Event 6", Date(), Role.GUEST),
                        EventInfo(6, "Event 7", Date(), Role.GUEST),
                    )
                ),
                {
                    if (isDarkModeEnabled) R.color.eerie_black
                    else R.color.whitesmoke
                },
                {
                    Log.w(LOG_TAG, "Logged Out")
                    true
                }
            )
        }
    }

    override fun getViewModelFactory(): HomeActivityViewModel.Factory {
        return getDummyFactory(isDarkModeEnabled())
    }

    override fun putSessionIdIntoIntent(intent: Intent) { }

    private fun isDarkModeEnabled(): Boolean {
        return (resources.configuration.uiMode and
                Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
    }
}