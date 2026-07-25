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

// version bumped 1 -> 2 for the new `category` column on FoodEntity.
@Database(entities = [FoodEntity::class, DailyEntryEntity::class], version = 2, exportSchema = false)
@TypeConverters(DateConverters::class)
abstract class NutritionDatabase : RoomDatabase() {
    abstract fun foodDao(): FoodDao
    abstract fun dailyEntryDao(): DailyEntryDao
    companion object {
        @Volatile private var INSTANCE: NutritionDatabase? = null
        fun get(context: Context): NutritionDatabase = INSTANCE ?: synchronized(this) {
            INSTANCE ?: Room.databaseBuilder(context.applicationContext, NutritionDatabase::class.java, "calburn.db")
                // Dev-stage convenience: wipes local data once on the schema bump above instead of
                // requiring a hand-written Migration. Fine while the app isn't shipped/has real users.
                .fallbackToDestructiveMigration(true)
                .build()
                .also { INSTANCE = it }
        }
    }
}