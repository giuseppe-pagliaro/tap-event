package com.giuseppepagliaro.tapevent.daos

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.giuseppepagliaro.tapevent.entities.InternalUser

@Dao
interface InternalUserDao {
    @Query("SELECT u.profilePic " +
            "FROM user AS u " +
            "WHERE u.cod = :cod"
    )
    fun getProfilePicByCod(cod: Long): LiveData<String>?

    @Query("SELECT * " +
            "FROM user AS u " +
            "WHERE U.cod = :cod"
    )
    fun getByCod(cod: Long): InternalUser?

    @Insert
    fun insert(user: InternalUser): Long

    @Delete
    fun delete(user: InternalUser)
}