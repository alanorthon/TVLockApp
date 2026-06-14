package com.example.tvlockapp.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.tvlockapp.data.model.AppLimit

@Dao
interface AppLimitDao {

    @Query("SELECT * FROM app_limits WHERE packageName = :packageName")
    fun getAppLimit(packageName: String): AppLimit?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(appLimit: AppLimit)

    @Update
    fun update(appLimit: AppLimit)

    @Query("UPDATE app_limits SET isBlocked = 0")
    fun resetAllBlocks()

    @Query("UPDATE app_limits SET isBlocked = 0")
    fun resetAllAppLimits()
}