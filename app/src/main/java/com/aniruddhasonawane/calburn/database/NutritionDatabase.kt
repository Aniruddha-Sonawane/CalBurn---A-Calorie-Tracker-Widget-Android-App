package com.aniruddhasonawane.calburn.database

import android.content.Context
import androidx.room.Database
import androidx.room.migration.Migration
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.aniruddhasonawane.calburn.dao.DailyEntryDao
import com.aniruddhasonawane.calburn.dao.FoodDao
import com.aniruddhasonawane.calburn.entity.DailyEntryEntity
import com.aniruddhasonawane.calburn.entity.FoodEntity
import java.time.LocalDate

class DateConverters {
    @TypeConverter
    fun fromDate(date: LocalDate): String = date.toString()

    @TypeConverter
    fun toDate(value: String): LocalDate = LocalDate.parse(value)
}

private val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            "ALTER TABLE foods ADD COLUMN defaultGrams REAL NOT NULL DEFAULT 100.0"
        )
    }
}

@Database(
    entities = [FoodEntity::class, DailyEntryEntity::class],
    version = 3,
    exportSchema = false
)
@TypeConverters(DateConverters::class)
abstract class NutritionDatabase : RoomDatabase() {

    abstract fun foodDao(): FoodDao
    abstract fun dailyEntryDao(): DailyEntryDao

    companion object {
        @Volatile
        private var INSTANCE: NutritionDatabase? = null

        fun get(context: Context): NutritionDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    NutritionDatabase::class.java,
                    "calburn.db"
                )
                    .addMigrations(MIGRATION_2_3)
                    .fallbackToDestructiveMigration(true)
                    .build()
                    .also { INSTANCE = it }
            }
    }
}