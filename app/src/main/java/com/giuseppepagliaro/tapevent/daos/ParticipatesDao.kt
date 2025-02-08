package com.giuseppepagliaro.tapevent.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Upsert
import com.giuseppepagliaro.tapevent.entities.Participates

@Dao
interface ParticipatesDao {
    @Upsert
    fun upsert(participates: Participates)

    @Delete
    fun delete(participates: Participates)
}