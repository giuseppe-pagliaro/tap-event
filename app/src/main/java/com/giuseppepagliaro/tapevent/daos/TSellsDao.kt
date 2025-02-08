package com.giuseppepagliaro.tapevent.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import com.giuseppepagliaro.tapevent.entities.TSells

@Dao
interface TSellsDao {
    @Insert
    fun insert(tSells: TSells)

    @Delete
    fun delete(tSells: TSells)
}