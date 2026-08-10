package com.aniruddhasonawane.calburn.repository

import com.aniruddhasonawane.calburn.database.NutritionDatabase
import com.aniruddhasonawane.calburn.entity.DailyEntryEntity
import com.aniruddhasonawane.calburn.entity.FoodEntity
import com.aniruddhasonawane.calburn.model.DailyTotalRow
import com.aniruddhasonawane.calburn.model.NutritionTotals
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.util.Locale

class NutritionRepository(
    database: NutritionDatabase
) {

    private val foods = database.foodDao()
    private val entries = database.dailyEntryDao()

    fun observeFoods(): Flow<List<FoodEntity>> =
        foods.observeAll()

    fun observeRecentFoods(): Flow<List<FoodEntity>> =
        foods.observeRecent()

    fun observeCategories(): Flow<List<String>> =
        foods.observeCategories()

    fun observeTodayTotals(): Flow<NutritionTotals> =
        entries.observeTotals(LocalDate.now())

    fun observeEntriesForDate(
        date: LocalDate
    ): Flow<List<DailyEntryEntity>> =
        entries.observeEntriesForDate(date)

    fun observeAllDailyTotals(): Flow<List<DailyTotalRow>> =
        entries.observeAllDailyTotals()

    fun observeHistory(days: Int): Flow<List<DailyTotalRow>> =
        entries.observeDailyTotalsSince(
            LocalDate.now().minusDays(days.toLong())
        )

    suspend fun saveFood(
        food: FoodEntity
    ): Long =
        if (food.id == 0L) {
            foods.insert(food)
        } else {
            foods.update(food)
            food.id
        }

    suspend fun deleteFood(food: FoodEntity) {
        foods.delete(food)
    }

    suspend fun deleteEntry(id: Long) {
        entries.deleteEntry(id)
    }

    suspend fun addFood(
        food: FoodEntity,
        grams: Double
    ) {
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

        foods.markUsed(
            food.id,
            System.currentTimeMillis()
        )
    }

    suspend fun saveAndAdd(
        food: FoodEntity,
        grams: Double
    ) {
        val id = saveFood(food)

        addFood(
            food.copy(id = id),
            grams
        )
    }

    suspend fun buildCsvExport(): String {
        val allEntries = entries.getAllEntries()
        val allFoods = foods.getAllFoods()

        val csv = StringBuilder()

        csv.appendLine(
            listOf(
                "record_type",
                "id",
                "date",
                "food_id",
                "food_name",
                "grams",
                "calories",
                "protein_g",
                "fiber_g",
                "fat_g",
                "category",
                "calories_per_100g",
                "protein_per_100g",
                "fiber_per_100g",
                "fat_per_100g",
                "default_grams",
                "last_used_at",
                "created_at"
            ).joinToString(",")
        )

        allEntries.forEach { entry ->
            csv.appendLine(
                listOf(
                    "daily_entry",
                    entry.id.toString(),
                    entry.date.toString(),
                    entry.foodId.toString(),
                    entry.foodName.csvEscape(),
                    entry.grams.csvNumber(),
                    entry.calories.csvNumber(),
                    entry.protein.csvNumber(),
                    entry.fiber.csvNumber(),
                    entry.fat.csvNumber(),
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    entry.createdAt.toString()
                ).joinToString(",")
            )
        }

        allFoods.forEach { food ->
            csv.appendLine(
                listOf(
                    "food_library",
                    food.id.toString(),
                    "",
                    food.id.toString(),
                    food.name.csvEscape(),
                    "",
                    "",
                    "",
                    "",
                    "",
                    food.category.csvEscape(),
                    food.caloriesPer100g.csvNumber(),
                    food.proteinPer100g.csvNumber(),
                    food.fiberPer100g.csvNumber(),
                    food.fatPer100g.csvNumber(),
                    food.defaultGrams.csvNumber(),
                    food.lastUsedAt.toString(),
                    ""
                ).joinToString(",")
            )
        }

        return csv.toString()
    }

    private fun String.csvEscape(): String =
        "\"" + replace("\"", "\"\"") + "\""

    private fun Double.csvNumber(): String =
        String.format(Locale.US, "%.4f", this)
}