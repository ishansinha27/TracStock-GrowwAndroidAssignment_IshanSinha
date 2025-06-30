package com.example.tracstock.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.tracstock.data.local.entities.StockCacheEntity

@Dao
interface StockCacheDao {
    @Query("SELECT * FROM STOCK_CACHE WHERE type=:type")
    suspend fun getCachedData(type:String) : StockCacheEntity?
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCache(cache : StockCacheEntity)

}