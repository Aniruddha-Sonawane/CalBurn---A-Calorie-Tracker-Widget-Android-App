package com.aniruddhasonawane.calburn.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface FoodLibraryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFood(food: FoodLibraryEntity)

    @Update
    suspend fun updateFood(food: FoodLibraryEntity)

    @Delete
    suspend fun deleteFood(food: FoodLibraryEntity)

    @Query("SELECT * FROM food_library ORDER BY name ASC")
    fun getAllFoods(): Flow<List<FoodLibraryEntity>>

    @Query("SELECT * FROM food_library WHERE category = :category ORDER BY name ASC")
    fun getFoodsByCategory(category: FoodCategory): Flow<List<FoodLibraryEntity>>

    @Query("SELECT * FROM food_library WHERE isFavorite = 1 ORDER BY name ASC")
    fun getFavoriteFoods(): Flow<List<FoodLibraryEntity>>

    @Query("SELECT * FROM food_library WHERE name LIKE '%' || :query || '%' ORDER BY name ASC")
    fun searchFoods(query: String): Flow<List<FoodLibraryEntity>>

    @Query("DELETE FROM food_library")
    suspend fun deleteAllFoods()
}