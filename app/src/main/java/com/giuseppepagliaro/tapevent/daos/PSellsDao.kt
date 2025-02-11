package com.giuseppepagliaro.tapevent.daos

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.giuseppepagliaro.tapevent.entities.PSells
import com.giuseppepagliaro.tapevent.entities.Stand

@Dao
interface PSellsDao {
    @Query("SELECT * " +
            "FROM stand as s " +
            "WHERE (s.eventCod, s.number) IN (" +
                "SELECT p.standEventCod, p.standNumber " +
                "FROM p_sells AS p " +
                "WHERE p.productEventCod = :eventCod AND p.productNumber = :number" +
            ")"
    )
    fun getStandsThatSellsProduct(eventCod: Long, number: Int): LiveData<List<Stand>>

    @Insert
    fun insert(pSells: PSells)

    @Delete
    fun delete(pSells: PSells)
}