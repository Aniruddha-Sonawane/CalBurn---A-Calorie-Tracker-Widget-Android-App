package com.aniruddhasonawane.calburn.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "meal_entries")
data class MealEntryEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    /**
     * References FoodLibraryEntity.id
     */
    val foodId: Long,

    /**
     * Quantity consumed.
     *
     * Examples:
     * 500 ml milk
     * 4 eggs
     * 250 g chicken
     */
    val quantity: Double,

    /**
     * Time when the meal was logged.
     * Stored as Unix time in milliseconds.
     */
    val dateTime: Long
)