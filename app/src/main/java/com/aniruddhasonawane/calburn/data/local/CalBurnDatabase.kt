package com.aniruddhasonawane.calburn.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        FoodEntity::class,
        FoodLibraryEntity::class,
        MealEntryEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class CalBurnDatabase : RoomDatabase() {

    // Legacy
    abstract fun foodDao(): FoodDao

    // New architecture
    abstract fun foodLibraryDao(): FoodLibraryDao

    abstract fun mealEntryDao(): MealEntryDao
}