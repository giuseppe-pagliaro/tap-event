package com.giuseppepagliaro.tapevent.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.giuseppepagliaro.tapevent.entities.CPManages

@Dao
interface CpManagesDao {
    @Query("SELECT EXISTS(" +
            "SELECT * " +
            "FROM cp_manages AS m " +
            "WHERE m.user = :userCod AND m.eventCod = :eventCod)"
    )
    fun isCashier(userCod: Long, eventCod: Long): Boolean

    @Insert
    fun insert(cpManages: CPManages)

    @Delete
    fun delete(cpManages: CPManages)
}