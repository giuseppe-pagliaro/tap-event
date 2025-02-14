package com.giuseppepagliaro.tapevent.daos

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.giuseppepagliaro.tapevent.entities.Owns
import com.giuseppepagliaro.tapevent.models.TicketCount

@Dao
interface OwnsDao {
    companion object {
        private const val GET_BY_CUSTOMER_QUERY =
            "SELECT t.eventCod, t.name, o.count " +
                    "FROM ticket_type AS t, owns AS o " +
                    "WHERE " +
                        "t.eventCod = o.ticketTypeEventCod AND " +
                        "t.name = o.ticketTypeName AND " +
                        "o.customer = :id"
    }

    @Query(GET_BY_CUSTOMER_QUERY)
    fun getByCustomerLive(id: String): LiveData<List<TicketCount>>

    @Query(GET_BY_CUSTOMER_QUERY)
    fun getByCustomer(id: String): List<TicketCount>

    @Upsert
    fun upsert(owns: Owns)

    @Delete
    fun delete(owns: Owns)

    @Transaction
    fun applyTransaction(upsert: List<Owns>, delete: List<Owns>) {
        for (operation in upsert) {
            upsert(operation)
        }
        for (operation in delete) {
            delete(operation)
        }
    }
}