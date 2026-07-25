package com.aniruddhasonawane.calburn.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.aniruddhasonawane.calburn.data.local.FoodLibraryEntity
import com.aniruddhasonawane.calburn.data.repository.FoodLibraryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class FoodLibraryViewModel(
    private val repository: FoodLibraryRepository
) : ViewModel() {

    private val _foods = MutableStateFlow<List<FoodLibraryEntity>>(emptyList())
    val foods: StateFlow<List<FoodLibraryEntity>> =
        _foods.asStateFlow()

    init {
        repository.getAllFoods()
            .onEach { foodList ->
                _foods.value = foodList
            }
            .launchIn(viewModelScope)
    }

    fun setFavorite(food: FoodLibraryEntity, isFavorite: Boolean) {
        viewModelScope.launch {
            repository.updateFood(food.copy(isFavorite = isFavorite))
        }
    }
}

class FoodLibraryViewModelFactory(
    private val repository: FoodLibraryRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(FoodLibraryViewModel::class.java)) {
            "Unknown ViewModel class: ${modelClass.name}"
        }
        return FoodLibraryViewModel(repository) as T
    }
}
