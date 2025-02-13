package com.giuseppepagliaro.tapevent.viewmodels

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.giuseppepagliaro.tapevent.models.EventInfo
import com.giuseppepagliaro.tapevent.models.Role
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class HomeActivityViewModel(
    private val getUsername: suspend () -> LiveData<String>,
    private val getProfilePic: suspend () -> LiveData<Uri>,
    private val getEvents: suspend () -> LiveData<List<EventInfo>>,

    val getRoleColor: (Role) -> Int,
    private val logoutRequest: suspend () -> Boolean
) : ViewModel() {
    private val _logoutEvent: MutableLiveData<Boolean> = MutableLiveData()

    val username: LiveData<String>
    val profilePic: LiveData<Uri>
    val events: LiveData<List<EventInfo>>
    val logoutEvent: LiveData<Boolean> = _logoutEvent

    init {
        val username: LiveData<String>
        val profilePic: LiveData<Uri>
        val events: LiveData<List<EventInfo>>

        runBlocking {
            username = getUsername()
            profilePic = getProfilePic()
            events = getEvents()
        }

        this.username = username
        this.profilePic = profilePic
        this.events = events
    }

    fun logout() {
        viewModelScope.launch {
            val success = logoutRequest()
            _logoutEvent.postValue(success)
        }
    }

    class Factory(
        private val getUsername: suspend () -> LiveData<String>,
        private val getProfilePic: suspend () -> LiveData<Uri>,
        private val getEvents: suspend () -> LiveData<List<EventInfo>>,
        private val getRoleColor: (Role) -> Int,
        private val logoutRequest: suspend () -> Boolean
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(HomeActivityViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return HomeActivityViewModel(
                    getUsername,
                    getProfilePic,
                    getEvents,
                    getRoleColor,
                    logoutRequest
                ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}