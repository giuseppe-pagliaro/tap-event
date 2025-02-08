package com.giuseppepagliaro.tapevent.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import com.giuseppepagliaro.tapevent.entities.SManages

@Dao
interface SManagesDao {
    @Insert
    fun insert(sManages: SManages)

    @Delete
    fun delete(sManages: SManages)
}