package com.example.tvlockapp.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.tvlockapp.data.model.AppLimit


import com.example.tvlockapp.data.dao.AppLimitDao

@Database(entities = [AppLimit::class], version = 3, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {


            abstract fun appLimitDao(): AppLimitDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                ).fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }


    }
}