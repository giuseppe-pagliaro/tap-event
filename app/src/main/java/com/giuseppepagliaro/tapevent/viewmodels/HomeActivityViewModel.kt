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

class HomeActivityViewModel(
    val username: LiveData<String>,
    val profilePic: LiveData<Uri>,
    val events: LiveData<List<EventInfo>>,

    val getRoleColor: (Role) -> Int,
    private val logoutRequest: suspend () -> Boolean
) : ViewModel() {
    private val _logoutEvent: MutableLiveData<Boolean> = MutableLiveData()
    val logoutEvent: LiveData<Boolean> = _logoutEvent

    fun logout() {
        viewModelScope.launch {
            val success = logoutRequest()
            _logoutEvent.postValue(success)
        }
    }

    class Factory(
        private val username: LiveData<String>,
        private val profilePic: LiveData<Uri>,
        private val events: LiveData<List<EventInfo>>,

        private val getRoleColor: (Role) -> Int,
        private val logoutRequest: suspend () -> Boolean
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(HomeActivityViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return HomeActivityViewModel(
                    username,
                    profilePic,
                    events,
                    getRoleColor,
                    logoutRequest
                ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}