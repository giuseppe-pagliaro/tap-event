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
    @Query("SELECT p.name, p.thumbnail AS thumbnailUri, x.ticketName, x.priceTickets " +
            "FROM product AS p, p_sells AS x " +
            "WHERE " +
                "p.eventCod = x.productEventCod AND p.name = x.productName AND " +
                "x.standEventCod = :eventCod AND x.standName = :name"
    )
    fun getByStand(eventCod: Long, name: String): List<ProductInfo>

    @Query("SELECT s.eventCod, s.name " +
            "FROM stand AS s, p_sells AS x " +
            "WHERE " +
                "s.eventCod = x.standEventCod AND s.name = x.standName AND " +
                "x.productEventCod = :eventCod AND x.productName = :name"
    )
    fun getStandsThatSellsProduct(eventCod: Long, name: String): LiveData<List<Stand>>

    @Insert
    fun insert(pSells: PSells): Long?

    @Delete
    fun delete(pSells: PSells)
}