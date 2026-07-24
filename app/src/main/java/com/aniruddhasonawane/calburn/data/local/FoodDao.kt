package com.aniruddhasonawane.calburn.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface FoodDao {

    @Insert
    suspend fun insertFood(food: FoodEntity)

    @Update
    suspend fun updateFood(food: FoodEntity)

    @Delete
    suspend fun deleteFood(food: FoodEntity)

    @Query("SELECT * FROM food_entries ORDER BY date DESC")
    fun getAllFoods(): Flow<List<FoodEntity>>

    @Query("SELECT * FROM food_entries WHERE date = :date")
    fun getFoodsByDate(date: Long): Flow<List<FoodEntity>>

    @Query("DELETE FROM food_entries")
    suspend fun deleteAllFoods()
}