package com.aniruddhasonawane.calburn.data.local

class FoodSeeder(
    private val foodLibraryDao: FoodLibraryDao
) {

    suspend fun seedIfEmpty() {

        val existingFoods = foodLibraryDao.getFoodCount()

        if (existingFoods == 0) {
            StarterFoods.foods.forEach { food ->
                foodLibraryDao.insertFood(food)
            }
        }
    }
}