package com.giuseppepagliaro.tapevent.daos

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.giuseppepagliaro.tapevent.entities.CPManages
import com.giuseppepagliaro.tapevent.entities.CashPoint

@Dao
interface CpManagesDao {
    @Query("SELECT c.eventCod, c.name " +
            "FROM cash_point AS c, cp_manages AS x " +
            "WHERE " +
                "c.eventCod = x.eventCod AND " +
                "c.name = x.cashPointName AND " +
                "x.eventCod = :eventCod AND " +
                "x.user = :userCod"
    )
    fun getCashPointsManagedByUser(eventCod: Long, userCod: Long): LiveData<List<CashPoint>>

    @Query("SELECT EXISTS(" +
            "SELECT * " +
            "FROM cp_manages AS m " +
            "WHERE m.user = :userCod AND m.eventCod = :eventCod)"
    )
    fun isCashier(eventCod: Long, userCod: Long): Boolean

    @Insert
    fun insert(cpManages: CPManages)

    @Delete
    fun delete(cpManages: CPManages)
}