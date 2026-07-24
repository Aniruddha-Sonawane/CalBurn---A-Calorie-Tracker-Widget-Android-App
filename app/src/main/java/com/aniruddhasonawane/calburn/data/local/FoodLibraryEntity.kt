package com.aniruddhasonawane.calburn.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "food_library")
data class FoodLibraryEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val name: String,

    val category: FoodCategory,

    /**
     * Example:
     * 100
     * 1
     * 250
     */
    val servingSize: Double,

    /**
     * Example:
     * g
     * ml
     * piece
     */
    val servingUnit: String,

    val calories: Double,

    val protein: Double,

    val fat: Double,

    val carbohydrates: Double,

    val fiber: Double,

    val sugar: Double = 0.0,

    val sodium: Double = 0.0,

    val potassium: Double = 0.0,

    val calcium: Double = 0.0,

    val iron: Double = 0.0,

    val vitaminC: Double = 0.0,

    val vitaminD: Double = 0.0,

    val vitaminB12: Double = 0.0,

    val isFavorite: Boolean = false,

    val isBuiltIn: Boolean = true
)