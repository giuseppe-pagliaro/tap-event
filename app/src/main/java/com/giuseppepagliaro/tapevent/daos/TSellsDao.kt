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
                "t.eventCod = s.ticketTypeEventCod AND t.name = s.ticketTypeName AND " +
                "s.cashPointEventCod = :eventCod AND s.cashPointName = :name"
    )
    fun getByCashPoint(eventCod: Long, name: String): List<TicketType>

    @Query("SELECT * " +
            "FROM cash_point AS c, t_sells AS s " +
            "WHERE " +
                "c.eventCod = s.cashPointEventCod AND c.name = s.cashPointName AND " +
                "s.ticketTypeEventCod = :eventCod AND s.ticketTypeName = :name"
    )
    fun getCashPointsThatSellsTicket(eventCod: Long, name: String): LiveData<List<CashPoint>>

    @Insert
    fun insert(tSells: TSells): Long?

    @Delete
    fun delete(tSells: TSells)
}