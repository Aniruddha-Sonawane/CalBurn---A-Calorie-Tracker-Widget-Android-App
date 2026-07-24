package com.aniruddhasonawane.calburn.data.repository

import com.aniruddhasonawane.calburn.data.local.FoodCategory
import com.aniruddhasonawane.calburn.data.local.FoodLibraryDao
import com.aniruddhasonawane.calburn.data.local.FoodLibraryEntity
import kotlinx.coroutines.flow.Flow

class FoodLibraryRepository(
    private val foodLibraryDao: FoodLibraryDao
) {

    fun getAllFoods(): Flow<List<FoodLibraryEntity>> =
        foodLibraryDao.getAllFoods()

    fun searchFoods(query: String): Flow<List<FoodLibraryEntity>> =
        foodLibraryDao.searchFoods(query)

    fun getFavoriteFoods(): Flow<List<FoodLibraryEntity>> =
        foodLibraryDao.getFavoriteFoods()

    fun getFoodsByCategory(category: FoodCategory): Flow<List<FoodLibraryEntity>> =
        foodLibraryDao.getFoodsByCategory(category)

    suspend fun insertFood(food: FoodLibraryEntity) =
        foodLibraryDao.insertFood(food)

    suspend fun updateFood(food: FoodLibraryEntity) =
        foodLibraryDao.updateFood(food)

    suspend fun deleteFood(food: FoodLibraryEntity) =
        foodLibraryDao.deleteFood(food)

    suspend fun deleteAllFoods() =
        foodLibraryDao.deleteAllFoods()
}