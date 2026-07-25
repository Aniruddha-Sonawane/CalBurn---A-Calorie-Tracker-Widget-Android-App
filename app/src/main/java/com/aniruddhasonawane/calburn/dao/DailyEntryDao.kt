package com.aniruddhasonawane.calburn.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.aniruddhasonawane.calburn.entity.DailyEntryEntity
import com.aniruddhasonawane.calburn.model.DailyTotalRow
import com.aniruddhasonawane.calburn.model.NutritionTotals
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface DailyEntryDao {
    @Insert suspend fun insert(entry: DailyEntryEntity)

    @Query("SELECT COALESCE(SUM(calories), 0) calories, COALESCE(SUM(protein), 0) protein, COALESCE(SUM(fiber), 0) fiber, COALESCE(SUM(fat), 0) fat FROM daily_entries WHERE date = :date")
    fun observeTotals(date: LocalDate): Flow<NutritionTotals>

    @Query("SELECT * FROM daily_entries WHERE date = :date ORDER BY createdAt DESC")
    fun observeEntriesForDate(date: LocalDate): Flow<List<DailyEntryEntity>>

    @Query("SELECT date, COALESCE(SUM(calories),0) calories, COALESCE(SUM(protein),0) protein, COALESCE(SUM(fiber),0) fiber, COALESCE(SUM(fat),0) fat FROM daily_entries WHERE date >= :sinceDate GROUP BY date ORDER BY date DESC")
    fun observeDailyTotalsSince(sinceDate: LocalDate): Flow<List<DailyTotalRow>>

    @Query("DELETE FROM daily_entries WHERE id = :id")
    suspend fun deleteEntry(id: Long)
}