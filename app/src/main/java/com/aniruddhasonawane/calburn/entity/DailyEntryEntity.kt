package com.aniruddhasonawane.calburn.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "daily_entries")
data class DailyEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val foodId: Long,
    val foodName: String,
    val date: LocalDate,
    val grams: Double,
    val calories: Double,
    val protein: Double,
    val fiber: Double,
    val fat: Double,
    val createdAt: Long = System.currentTimeMillis()
)
