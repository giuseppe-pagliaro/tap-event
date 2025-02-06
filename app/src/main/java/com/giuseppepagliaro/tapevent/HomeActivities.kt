package com.giuseppepagliaro.tapevent

import android.content.res.Configuration
import android.net.Uri
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.giuseppepagliaro.tapevent.dto.EventDto
import com.giuseppepagliaro.tapevent.entities.Role
import com.giuseppepagliaro.tapevent.viewmodels.HomeActivityViewModel
import java.util.Date

class DummyHomeActivity : HomeActivity() {
    companion object {
        private const val LOG_TAG = "DummyHomeActivity"
    }

    override fun getViewModelFactory(): HomeActivityViewModel.Factory {
        return HomeActivityViewModel.Factory(
            MutableLiveData("User"),
            MutableLiveData(Uri.parse(
                "android.resource://com.giuseppepagliaro.tapevent/drawable/profile"
            )),
            MutableLiveData(listOf(
                EventDto(0, "Event 1", Date()),
                EventDto(1, "Event 2", Date()),
                EventDto(2, "Event 3", Date()),
                EventDto(3, "Event 4", Date()),
                EventDto(4, "Event 5", Date()),
                EventDto(5, "Event 6", Date()),
                EventDto(6, "Event 7", Date()),
            )),
            { Role.GUEST },
            {
                if (isDarkModeEnabled()) R.color.eerie_black
                else R.color.whitesmoke
            },
            {
                Log.w(LOG_TAG,"Logged Out")
                true
            }
        )
    }

    private fun isDarkModeEnabled(): Boolean {
        return (resources.configuration.uiMode and
                Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
    }
}