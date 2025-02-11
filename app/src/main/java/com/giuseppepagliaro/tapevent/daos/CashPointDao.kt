package com.giuseppepagliaro.tapevent.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.giuseppepagliaro.tapevent.entities.CashPoint

@Dao
interface CashPointDao {
    @Query("SELECT * " +
            "FROM cash_point AS c " +
            "WHERE c.eventCod = :eventCod")
    fun getAll(eventCod: Long): List<CashPoint>

    @Query("SELECT c.number " +
            "FROM cash_point AS c " +
            "WHERE c.eventCod = :eventCod")
    fun getAllNumbers(eventCod: Long): List<Int>

    @Insert
    fun insert(cashPoint: CashPoint): Long?

    @Update
    fun update(cashPoint: CashPoint): Int?

    @Delete
    fun delete(cashPoint: CashPoint)
}