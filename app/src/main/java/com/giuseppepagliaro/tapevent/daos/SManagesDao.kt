package com.giuseppepagliaro.tapevent.daos

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.giuseppepagliaro.tapevent.entities.SManages
import com.giuseppepagliaro.tapevent.entities.Stand

@Dao
interface SManagesDao {
    @Query("SELECT s.eventCod, s.name " +
            "FROM stand AS s, s_manages AS x " +
            "WHERE " +
                "s.eventCod = x.eventCod AND s.name = x.standName AND " +
                "x.eventCod = :eventCod AND x.user = :userCod"
    )
    fun getStandsManagedByUser(eventCod: Long, userCod: Long): LiveData<List<Stand>>

    @Query("SELECT EXISTS(" +
            "SELECT * " +
            "FROM s_manages AS s " +
            "WHERE s.eventCod = :eventCod AND s.user = :userCod)"
    )
    fun isStander(eventCod: Long, userCod: Long): Boolean

    @Insert
    fun insert(sManages: SManages)

    @Delete
    fun delete(sManages: SManages)
}