package com.giuseppepagliaro.tapevent.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import com.giuseppepagliaro.tapevent.entities.PSells

@Dao
interface PSellsDao {
    @Insert
    fun insert(pSells: PSells)

    @Delete
    fun delete(pSells: PSells)
}