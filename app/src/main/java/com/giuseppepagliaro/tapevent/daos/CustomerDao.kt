package com.giuseppepagliaro.tapevent.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import com.giuseppepagliaro.tapevent.entities.Customer

@Dao
interface CustomerDao {
    @Query("SELECT * " +
            "FROM customer AS c " +
            "WHERE c.id = :customerId"
    )
    fun getByCustomerId(customerId: String): Customer

    @Upsert
    fun upsert(customer: Customer): Long?

    @Delete
    fun delete(customer: Customer)
}