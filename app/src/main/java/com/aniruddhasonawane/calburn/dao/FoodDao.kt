package com.aniruddhasonawane.calburn.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.aniruddhasonawane.calburn.entity.FoodEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FoodDao {
    @Query("SELECT * FROM foods ORDER BY name COLLATE NOCASE") fun observeAll(): Flow<List<FoodEntity>>
    @Query("SELECT * FROM foods ORDER BY lastUsedAt DESC LIMIT :limit") fun observeRecent(limit: Int = 8): Flow<List<FoodEntity>>
    @Query("SELECT DISTINCT category FROM foods WHERE category != '' ORDER BY category COLLATE NOCASE") fun observeCategories(): Flow<List<String>>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insert(food: FoodEntity): Long
    @Update suspend fun update(food: FoodEntity)
    @Delete suspend fun delete(food: FoodEntity)
    @Query("UPDATE foods SET lastUsedAt = :timestamp WHERE id = :foodId") suspend fun markUsed(foodId: Long, timestamp: Long)
}