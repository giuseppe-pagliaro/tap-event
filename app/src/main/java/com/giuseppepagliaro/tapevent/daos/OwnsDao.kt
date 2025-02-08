package com.giuseppepagliaro.tapevent.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Upsert
import com.giuseppepagliaro.tapevent.entities.Owns

@Dao
interface OwnsDao {
    @Upsert
    fun upsert(owns: Owns)

    @Delete
    fun delete(owns: Owns)
}