package com.giuseppepagliaro.tapevent.daos

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.giuseppepagliaro.tapevent.entities.CashPoint
import com.giuseppepagliaro.tapevent.entities.TSells
import com.giuseppepagliaro.tapevent.entities.TicketType

@Dao
interface TSellsDao {
    @Query("SELECT t.eventCod, t.name, t.price " +
            "FROM ticket_type AS t, t_sells AS s " +
            "WHERE " +
                "(t.eventCod, t.name) = (s.ticketTypeEventCod, s.ticketTypeName) AND " +
                "(s.cashPointEventCod, s.cashPointName) = (:eventCod, :name)"
    )
    fun getByCashPoint(eventCod: Long, name: String): List<TicketType>

    @Query("SELECT * " +
            "FROM cash_point AS c, t_sells AS s " +
            "WHERE " +
                "(c.eventCod, c.name) = (s.cashPointEventCod, s.cashPointName) AND " +
                "(s.ticketTypeEventCod, s.ticketTypeName) = (:eventCod, :name)"
    )
    fun getCashPointsThatSellsTicket(eventCod: Long, name: String): LiveData<List<CashPoint>>

    @Insert
    fun insert(tSells: TSells): Long?

    @Delete
    fun delete(tSells: TSells)
}