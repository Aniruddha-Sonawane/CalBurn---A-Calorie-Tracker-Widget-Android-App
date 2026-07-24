package com.aniruddhasonawane.calburn.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "food_entries")
data class FoodEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val foodName: String,

    val calories: Int,

    val protein: Double,

    val fat: Double,

    val fiber: Double,

    val quantity: String,

    val date: Long
)