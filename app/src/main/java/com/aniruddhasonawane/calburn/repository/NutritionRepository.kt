package com.aniruddhasonawane.calburn.repository

import com.aniruddhasonawane.calburn.database.NutritionDatabase
import com.aniruddhasonawane.calburn.entity.DailyEntryEntity
import com.aniruddhasonawane.calburn.entity.FoodEntity
import com.aniruddhasonawane.calburn.model.DailyTotalRow
import com.aniruddhasonawane.calburn.model.NutritionTotals
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

class NutritionRepository(database: NutritionDatabase) {
    private val foods = database.foodDao()
    private val entries = database.dailyEntryDao()

    fun observeFoods(): Flow<List<FoodEntity>> = foods.observeAll()
    fun observeRecentFoods(): Flow<List<FoodEntity>> = foods.observeRecent()
    fun observeCategories(): Flow<List<String>> = foods.observeCategories()
    fun observeTodayTotals(): Flow<NutritionTotals> = entries.observeTotals(LocalDate.now())
    fun observeEntriesForDate(date: LocalDate): Flow<List<DailyEntryEntity>> = entries.observeEntriesForDate(date)
    fun observeHistory(days: Int): Flow<List<DailyTotalRow>> = entries.observeDailyTotalsSince(LocalDate.now().minusDays(days.toLong()))

    suspend fun saveFood(food: FoodEntity): Long = if (food.id == 0L) foods.insert(food) else { foods.update(food); food.id }
    suspend fun deleteFood(food: FoodEntity) = foods.delete(food)
    suspend fun deleteEntry(id: Long) = entries.deleteEntry(id)

    suspend fun addFood(food: FoodEntity, grams: Double) {
        val factor = grams / 100.0
        entries.insert(
            DailyEntryEntity(
                foodId = food.id,
                foodName = food.name,
                date = LocalDate.now(),
                grams = grams,
                calories = food.caloriesPer100g * factor,
                protein = food.proteinPer100g * factor,
                fiber = food.fiberPer100g * factor,
                fat = food.fatPer100g * factor
            )
        )
        foods.markUsed(food.id, System.currentTimeMillis())
    }

    suspend fun saveAndAdd(food: FoodEntity, grams: Double) { val id = saveFood(food); addFood(food.copy(id = id), grams) }
}