package com.giuseppepagliaro.tapevent.daos

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.giuseppepagliaro.tapevent.entities.PSells
import com.giuseppepagliaro.tapevent.entities.Stand
import com.giuseppepagliaro.tapevent.models.ProductInfo

@Dao
interface PSellsDao {
    @Query("SELECT p.name, p.thumbnail, x.ticketName, x.priceTickets " +
            "FROM product AS p, p_sells AS x " +
            "WHERE " +
                "(p.eventCod, p.name) = (x.productEventCod, x.productName) AND " +
                "(x.standEventCod, x.standName) = (:eventCod, :name)"
    )
    fun getByStand(eventCod: Long, name: String): List<ProductInfo>

    @Query("SELECT s.eventCod, s.name " +
            "FROM stand AS s, p_sells AS x " +
            "WHERE " +
                "(s.eventCod, s.name) = (x.standEventCod, x.standName) AND " +
                "(x.productEventCod, x.productName) = (:eventCod, :name)"
    )
    fun getStandsThatSellsProduct(eventCod: Long, name: String): LiveData<List<Stand>>

    @Insert
    fun insert(pSells: PSells): Long?

    @Delete
    fun delete(pSells: PSells)
}