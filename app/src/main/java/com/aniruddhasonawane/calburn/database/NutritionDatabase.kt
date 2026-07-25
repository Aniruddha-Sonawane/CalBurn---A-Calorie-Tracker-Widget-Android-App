package com.aniruddhasonawane.calburn.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.aniruddhasonawane.calburn.dao.DailyEntryDao
import com.aniruddhasonawane.calburn.dao.FoodDao
import com.aniruddhasonawane.calburn.entity.DailyEntryEntity
import com.aniruddhasonawane.calburn.entity.FoodEntity
import java.time.LocalDate

class DateConverters { @TypeConverter fun fromDate(date: LocalDate): String = date.toString(); @TypeConverter fun toDate(value: String): LocalDate = LocalDate.parse(value) }
@Database(entities = [FoodEntity::class, DailyEntryEntity::class], version = 1, exportSchema = false)
@TypeConverters(DateConverters::class)
abstract class NutritionDatabase : RoomDatabase() {
    abstract fun foodDao(): FoodDao
    abstract fun dailyEntryDao(): DailyEntryDao
    companion object { @Volatile private var INSTANCE: NutritionDatabase? = null
        fun get(context: Context): NutritionDatabase = INSTANCE ?: synchronized(this) { INSTANCE ?: Room.databaseBuilder(context.applicationContext, NutritionDatabase::class.java, "calburn.db").build().also { INSTANCE = it } }
    }
}
