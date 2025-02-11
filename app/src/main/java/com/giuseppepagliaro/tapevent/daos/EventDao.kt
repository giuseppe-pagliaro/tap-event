package com.giuseppepagliaro.tapevent.daos

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.giuseppepagliaro.tapevent.entities.Event

@Dao
interface EventDao {
    @Query("SELECT * " +
            "FROM event"
    )
    fun getAll(): LiveData<List<Event>>

    @Query("SELECT * " +
            "FROM event AS e " +
            "WHERE e.cod = :cod"
    )
    fun getByCod(cod: Long): LiveData<Event>?

    @Query("SELECT COUNT(*) > 0 " +
            "FROM event AS e " +
            "WHERE e.owner = :userCod AND e.cod = :eventCod"
    )
    fun isOwner(userCod: Long, eventCod: Long): Boolean

    @Insert
    fun insert(event: Event): Long?

    @Update
    fun update(event: Event)

    @Delete
    fun delete(event: Event)
}