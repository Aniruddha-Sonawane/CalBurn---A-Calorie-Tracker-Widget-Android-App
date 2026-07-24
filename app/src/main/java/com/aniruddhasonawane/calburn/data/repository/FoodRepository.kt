package com.aniruddhasonawane.calburn.data.repository

import com.aniruddhasonawane.calburn.data.local.FoodDao
import com.aniruddhasonawane.calburn.data.local.FoodEntity
import kotlinx.coroutines.flow.Flow

class FoodRepository(
    private val foodDao: FoodDao
) {

    fun getAllFoods(): Flow<List<FoodEntity>> {
        return foodDao.getAllFoods()
    }

    fun getFoodsByDate(date: Long): Flow<List<FoodEntity>> {
        return foodDao.getFoodsByDate(date)
    }

    suspend fun insertFood(food: FoodEntity) {
        foodDao.insertFood(food)
    }

    suspend fun updateFood(food: FoodEntity) {
        foodDao.updateFood(food)
    }

    suspend fun deleteFood(food: FoodEntity) {
        foodDao.deleteFood(food)
    }

    suspend fun deleteAllFoods() {
        foodDao.deleteAllFoods()
    }
}