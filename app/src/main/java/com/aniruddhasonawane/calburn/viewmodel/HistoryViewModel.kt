package com.aniruddhasonawane.calburn.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.aniruddhasonawane.calburn.entity.DailyEntryEntity
import com.aniruddhasonawane.calburn.model.DailyTotalRow
import com.aniruddhasonawane.calburn.repository.NutritionRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

data class HistoryState(
    val history: List<DailyTotalRow> = emptyList(),
    val selectedDate: LocalDate = LocalDate.now(),
    val selectedEntries: List<DailyEntryEntity> = emptyList()
)

class HistoryViewModel(private val repository: NutritionRepository) : ViewModel() {
    private val selectedDate = MutableStateFlow(LocalDate.now())
    private val history = repository.observeHistory(30)

    @OptIn(ExperimentalCoroutinesApi::class)
    private val entriesForSelected = selectedDate.flatMapLatest { repository.observeEntriesForDate(it) }

    val state: StateFlow<HistoryState> = combine(history, selectedDate, entriesForSelected) { h, d, e ->
        HistoryState(history = h, selectedDate = d, selectedEntries = e)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HistoryState())

    fun selectDate(date: LocalDate) {
        // Tapping the already-expanded day collapses it again.
        selectedDate.value = if (selectedDate.value == date) LocalDate.MIN else date
    }

    fun deleteEntry(id: Long) = viewModelScope.launch { repository.deleteEntry(id) }
}

class HistoryViewModelFactory(private val repository: NutritionRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = HistoryViewModel(repository) as T
}