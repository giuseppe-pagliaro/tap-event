package com.giuseppepagliaro.tapevent.repositories

class UserPermissionRepository {
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