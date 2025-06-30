package com.example.tracstock.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.tracstock.data.local.entities.HistoricalDataCacheEntity

@Dao
interface HistoricalDataDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistoricalData(entity: HistoricalDataCacheEntity)

    @Query("SELECT * FROM historical_data_cache WHERE symbol = :symbol")
    suspend fun getHistoricalData(symbol: String): HistoricalDataCacheEntity?
}