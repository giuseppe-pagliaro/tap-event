package com.giuseppepagliaro.tapevent.users

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface SessionDao {
    @Query("SELECT s.user " +
            "FROM session AS s " +
            "WHERE s.id = :sessionId"
    )
    fun getInternalCodBySession(sessionId: String): Long?

    @Insert
    fun insert(session: Session): Long

    @Delete
    fun delete(session: Session)
}