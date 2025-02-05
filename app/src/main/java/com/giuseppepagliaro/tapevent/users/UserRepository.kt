package com.giuseppepagliaro.tapevent.users

class UserRepository {
    fun login(username: String, password: String): String {
        return "" //TODO
    }

    fun logout(sessionCod: String): Boolean {
        return false //TODO
    }

    fun getInternalCodBySession(sessionCod: String): Int? {
        return null // TODO
    }

    fun isOwner(userId: Int, eventId: Int): Boolean {
        return false // TODO
    }

    fun isAdmin(userId: Int, eventId: Int): Boolean {
        return false // TODO
    }

    fun isCashier(userId: Int, eventId: Int, cashpointId: Int): Boolean {
        return false // TODO
    }

    fun isStandKeeper(userId: Int, eventId: Int, standId: Int): Boolean {
        return false // TODO
    }

    fun isGuest(userId: Int, eventId: Int): Boolean {
        return false // TODO
    }
}