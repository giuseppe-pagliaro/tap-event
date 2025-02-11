package com.giuseppepagliaro.tapevent.users

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface UserDao {
    @Query("SELECT u.internalCod " +
            "FROM user_credentials AS u " +
            "WHERE u.username = :username AND u.password = :password"
    )
    fun getInternalCodByUserAndPwd(username: String, password: String): Long?

    @Query("SELECT u.internalCod " +
            "FROM user_credentials AS u " +
            "WHERE u.username = :username"
    )
    fun getInternalCodByUsername(username: String): Long?

    @Query("SELECT u.username " +
            "FROM user_credentials AS u " +
            "WHERE u.internalCod = :cod"
    )
    fun getUsernameByCod(cod: Long): LiveData<String>?

    @Query("SELECT * " +
            "FROM user_credentials AS u " +
            "WHERE u.internalCod = :cod"
    )
    fun getByCod(cod: Long): User?

    @Insert
    fun insert(user: User): Long

    @Delete
    fun delete(user: User)
}