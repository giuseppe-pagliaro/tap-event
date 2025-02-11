package com.giuseppepagliaro.tapevent.daos

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.giuseppepagliaro.tapevent.entities.Product

@Dao
interface ProductDao {
    @Query("SELECT * " +
            "FROM product AS p " +
            "WHERE p.eventCod = :eventCod"
    )
    fun getByEvent(eventCod: Long): LiveData<List<Product>>

    @Insert
    fun insert(product: Product): Long?

    @Update
    fun update(product: Product): Int?

    @Delete
    fun delete(product: Product)
}