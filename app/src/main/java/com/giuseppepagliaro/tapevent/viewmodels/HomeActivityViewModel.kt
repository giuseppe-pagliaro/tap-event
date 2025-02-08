package com.giuseppepagliaro.tapevent.viewmodels

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.giuseppepagliaro.tapevent.models.EventInfo
import com.giuseppepagliaro.tapevent.entities.Role

class HomeActivityViewModel(
    val username: LiveData<String>,
    val profilePic: LiveData<Uri>,
    val events: LiveData<List<EventInfo>>,

    val getRoleColor: (Role) -> Int,
    val onLogout: () -> Boolean
) : ViewModel() {
    class Factory(
        private val username: LiveData<String>,
        private val profilePic: LiveData<Uri>,
        private val events: LiveData<List<EventInfo>>,

        private val getRoleColor: (Role) -> Int,
        private val onLogout: () -> Boolean
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(HomeActivityViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return HomeActivityViewModel(
                    username,
                    profilePic,
                    events,
                    getRoleColor,
                    onLogout
                ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}