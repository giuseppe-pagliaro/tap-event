package com.giuseppepagliaro.tapevent.daos

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.giuseppepagliaro.tapevent.entities.CashPoint
import com.giuseppepagliaro.tapevent.entities.TSells

@Dao
interface TSellsDao {
    @Query("SELECT * " +
            "FROM cash_point as c " +
            "WHERE (c.eventCod, c.number) IN (" +
                "SELECT t.cashPointEventCod, t.cashPointNumber " +
                "FROM t_sells AS t " +
                "WHERE t.ticketTypeEventCod = :eventCod AND t.ticketTypeNumber = :number" +
            ")"
    )
    fun getCashpointsThatSellsTicket(eventCod: Long, number: Int): LiveData<List<CashPoint>>

    @Insert
    fun insert(tSells: TSells)

    @Delete
    fun delete(tSells: TSells)
}