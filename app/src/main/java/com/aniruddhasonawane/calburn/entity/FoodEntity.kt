package com.aniruddhasonawane.calburn.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "foods")
data class FoodEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val category: String = "General",
    val caloriesPer100g: Double,
    val proteinPer100g: Double,
    val fiberPer100g: Double,
    val fatPer100g: Double,
    val lastUsedAt: Long = 0L
)