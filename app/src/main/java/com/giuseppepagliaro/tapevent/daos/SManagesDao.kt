package com.giuseppepagliaro.tapevent.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.giuseppepagliaro.tapevent.entities.SManages

@Dao
interface SManagesDao {
    @Query("SELECT EXISTS(" +
            "SELECT * " +
            "FROM s_manages AS s " +
            "WHERE s.user = :userCod AND s.eventCod = :eventCod)"
    )
    fun isStander(userCod: Long, eventCod: Long): Boolean

    @Insert
    fun insert(sManages: SManages)

    @Delete
    fun delete(sManages: SManages)
}