package com.aniruddhasonawane.calburn.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.aniruddhasonawane.calburn.entity.FoodEntity
import com.aniruddhasonawane.calburn.repository.NutritionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

const val TAB_RECENT = "RECENT"
const val TAB_ALL = "ALL"

data class FoodPopupState(
    val foods: List<FoodEntity> = emptyList(),
    val recentFoods: List<FoodEntity> = emptyList(),
    val categories: List<String> = emptyList(),
    val query: String = "",
    val selectedTab: String = TAB_ALL
) {
    val displayedFoods: List<FoodEntity>
        get() = when {
            query.isNotBlank() -> foods.filter { it.name.contains(query, ignoreCase = true) }
            selectedTab == TAB_RECENT -> recentFoods
            selectedTab == TAB_ALL -> foods
            else -> foods.filter { it.category.equals(selectedTab, ignoreCase = true) }
        }
}

class FoodPopupViewModel(private val repository: NutritionRepository) : ViewModel() {
    private val query = MutableStateFlow("")
    private val selectedTab = MutableStateFlow(TAB_ALL)

    val state: StateFlow<FoodPopupState> = combine(
        repository.observeFoods(),
        repository.observeRecentFoods(),
        repository.observeCategories(),
        query,
        selectedTab
    ) { values ->
        @Suppress("UNCHECKED_CAST")
        FoodPopupState(
            foods = values[0] as List<FoodEntity>,
            recentFoods = values[1] as List<FoodEntity>,
            categories = values[2] as List<String>,
            query = values[3] as String,
            selectedTab = values[4] as String
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), FoodPopupState())

    fun search(value: String) { query.value = value }
    fun selectTab(tab: String) { selectedTab.value = tab }
    fun saveAndAdd(food: FoodEntity, grams: Double, complete: () -> Unit) = viewModelScope.launch { repository.saveAndAdd(food, grams); complete() }
    fun add(food: FoodEntity, grams: Double, complete: () -> Unit) = viewModelScope.launch { repository.addFood(food, grams); complete() }
    fun update(food: FoodEntity) = viewModelScope.launch { repository.saveFood(food) }
    fun delete(food: FoodEntity) = viewModelScope.launch { repository.deleteFood(food) }
}

class FoodPopupViewModelFactory(private val repository: NutritionRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST") override fun <T : ViewModel> create(modelClass: Class<T>): T = FoodPopupViewModel(repository) as T
}