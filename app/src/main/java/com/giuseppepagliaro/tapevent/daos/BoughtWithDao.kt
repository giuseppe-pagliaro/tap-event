package com.giuseppepagliaro.tapevent.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Update
import com.giuseppepagliaro.tapevent.entities.BoughtWith

@Dao
interface BoughtWithDao {
    @Insert
    fun insert(boughtWith: BoughtWith)

    @Update
    fun update(boughtWith: BoughtWith)

    @Delete
    fun delete(boughtWith: BoughtWith)
}