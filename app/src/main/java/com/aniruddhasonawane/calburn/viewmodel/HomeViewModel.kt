package com.aniruddhasonawane.calburn.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aniruddhasonawane.calburn.data.local.FoodEntity
import com.aniruddhasonawane.calburn.data.repository.FoodRepository
import com.aniruddhasonawane.calburn.model.NutritionSummary
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class HomeViewModel(
    private val repository: FoodRepository
) : ViewModel() {

    private val _nutritionSummary = MutableStateFlow(NutritionSummary())
    val nutritionSummary: StateFlow<NutritionSummary> =
        _nutritionSummary.asStateFlow()

    init {
        observeAllFoods()
    }

    private fun observeAllFoods() {
        repository.getAllFoods()
            .onEach { foods ->
                _nutritionSummary.value = calculateSummary(foods)
            }
            .launchIn(viewModelScope)
    }

    private fun calculateSummary(
        foods: List<FoodEntity>
    ): NutritionSummary {

        return NutritionSummary(
            calories = foods.sumOf { it.calories },
            protein = foods.sumOf { it.protein },
            fat = foods.sumOf { it.fat },
            fiber = foods.sumOf { it.fiber }
        )
    }

    suspend fun addFood(food: FoodEntity) {
        repository.insertFood(food)
    }

    suspend fun deleteFood(food: FoodEntity) {
        repository.deleteFood(food)
    }
}