package com.giuseppepagliaro.tapevent.daos

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.giuseppepagliaro.tapevent.entities.TicketType

@Dao
interface TicketTypeDao {
    @Query("SELECT * " +
            "FROM ticket_type AS t " +
            "WHERE t.eventCod = :eventCod AND t.name = :name"
    )
    fun getByEventAndName(eventCod: Long, name: String): LiveData<TicketType>

    @Query("SELECT * " +
            "FROM ticket_type AS t " +
            "WHERE t.eventCod = :eventCod"
    )
    fun getByEvent(eventCod: Long): LiveData<List<TicketType>>

    @Insert
    fun insert(ticketType: TicketType): Long?

    @Update
    fun update(ticketType: TicketType): Int?

    @Delete
    fun delete(ticketType: TicketType)
}