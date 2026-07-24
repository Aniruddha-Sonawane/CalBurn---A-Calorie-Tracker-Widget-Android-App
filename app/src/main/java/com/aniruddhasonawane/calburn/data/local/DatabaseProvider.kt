package com.aniruddhasonawane.calburn.data.local

import android.content.Context
import androidx.room.Room

object DatabaseProvider {

    @Volatile
    private var INSTANCE: CalBurnDatabase? = null

    fun getDatabase(context: Context): CalBurnDatabase {
        return INSTANCE ?: synchronized(this) {
            val instance = Room.databaseBuilder(
                context.applicationContext,
                CalBurnDatabase::class.java,
                "calburn_database"
            ).build()

            INSTANCE = instance
            instance
        }
    }
}