package com.giuseppepagliaro.tapevent.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import com.giuseppepagliaro.tapevent.entities.CPManages

@Dao
interface CpManagesDao {
    @Insert
    fun insert(cpManages: CPManages)

    @Delete
    fun delete(cpManages: CPManages)
}