package com.aniruddhasonawane.calburn.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.aniruddhasonawane.calburn.entity.FoodEntity
import com.aniruddhasonawane.calburn.repository.NutritionRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class FoodPopupState(val foods: List<FoodEntity> = emptyList(), val recentFoods: List<FoodEntity> = emptyList(), val query: String = "") {
    val filteredFoods get() = if (query.isBlank()) foods else foods.filter { it.name.contains(query, true) }
}
class FoodPopupViewModel(private val repository: NutritionRepository) : ViewModel() {
    private val query = kotlinx.coroutines.flow.MutableStateFlow("")
    val state: StateFlow<FoodPopupState> = combine(repository.observeFoods(), repository.observeRecentFoods(), query) { foods, recent, q -> FoodPopupState(foods, recent, q) }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), FoodPopupState())
    fun search(value: String) { query.value = value }
    fun saveAndAdd(food: FoodEntity, grams: Double, complete: () -> Unit) = viewModelScope.launch { repository.saveAndAdd(food, grams); complete() }
    fun add(food: FoodEntity, grams: Double, complete: () -> Unit) = viewModelScope.launch { repository.addFood(food, grams); complete() }
    fun update(food: FoodEntity) = viewModelScope.launch { repository.saveFood(food) }
    fun delete(food: FoodEntity) = viewModelScope.launch { repository.deleteFood(food) }
}
class FoodPopupViewModelFactory(private val repository: NutritionRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST") override fun <T : ViewModel> create(modelClass: Class<T>): T = FoodPopupViewModel(repository) as T
}
