package com.giuseppepagliaro.tapevent.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.giuseppepagliaro.tapevent.entities.Stand

@Dao
interface StandDao {
    @Query("SELECT * " +
            "FROM stand AS s " +
            "WHERE s.eventCod = :eventCod")
    fun getAll(eventCod: Long): List<Stand>

    @Query("SELECT s.number " +
            "FROM stand AS s " +
            "WHERE s.eventCod = :eventCod")
    fun getAllNumbers(eventCod: Long): List<Int>

    @Insert
    fun insert(stand: Stand): Long?

    @Update
    fun update(stand: Stand): Int?

    @Delete
    fun delete(stand: Stand)
}