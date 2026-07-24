package com.aniruddhasonawane.calburn.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MealEntryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeal(meal: MealEntryEntity)

    @Update
    suspend fun updateMeal(meal: MealEntryEntity)

    @Delete
    suspend fun deleteMeal(meal: MealEntryEntity)

    @Query("SELECT * FROM meal_entries ORDER BY dateTime DESC")
    fun getAllMeals(): Flow<List<MealEntryEntity>>

    @Query(
        """
        SELECT * FROM meal_entries
        WHERE dateTime BETWEEN :startOfDay AND :endOfDay
        ORDER BY dateTime DESC
        """
    )
    fun getMealsForDay(
        startOfDay: Long,
        endOfDay: Long
    ): Flow<List<MealEntryEntity>>

    @Query("DELETE FROM meal_entries")
    suspend fun deleteAllMeals()
}