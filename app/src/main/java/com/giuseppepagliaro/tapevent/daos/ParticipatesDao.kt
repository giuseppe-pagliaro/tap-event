package com.giuseppepagliaro.tapevent.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import com.giuseppepagliaro.tapevent.entities.Participates

@Dao
interface ParticipatesDao {

    // null -> does not participate; false -> guest; true -> admin
    @Query("SELECT p.isAdmin " +
            "FROM participates AS p " +
            "WHERE p.event = :eventCod AND p.user = :userCod"
    )
    fun getUserParticipationStatus(eventCod: Long, userCod: Long): Boolean?

    @Upsert
    fun upsert(participates: Participates)

    @Delete
    fun delete(participates: Participates)
}