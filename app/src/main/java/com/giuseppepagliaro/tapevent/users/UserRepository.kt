package com.giuseppepagliaro.tapevent.users

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class UserRepository {
    fun getSessionId(): String? {
        return "blabla"
    }

    fun login(username: String, password: String, rememberMe: Boolean): String? {
        return "" //TODO
    }

    fun logout(sessionId: String): Boolean {
        return true //TODO
    }

    fun getUserInfo(sessionId: String): LiveData<UserInfo>? {
        return MutableLiveData(UserInfo("User")) // TODO
    }
}