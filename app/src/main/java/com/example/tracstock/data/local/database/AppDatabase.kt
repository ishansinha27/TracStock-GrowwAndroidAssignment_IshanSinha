package com.example.tracstock.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.tracstock.data.local.dao.CompanyOverviewDao
import com.example.tracstock.data.local.dao.HistoricalDataDao
import com.example.tracstock.data.local.dao.StockCacheDao
import com.example.tracstock.data.local.dao.WatchlistDao
import com.example.tracstock.data.local.entities.CompanyOverviewCacheEntity
import com.example.tracstock.data.local.entities.HistoricalDataCacheEntity
import com.example.tracstock.data.local.entities.StockCacheEntity
import com.example.tracstock.data.local.entities.WatchlistEntity
import com.example.tracstock.data.local.entities.WatchlistItemEntity

@Database(
    entities = [
        StockCacheEntity::class,
        WatchlistEntity::class,
        WatchlistItemEntity::class,
        CompanyOverviewCacheEntity::class,
        HistoricalDataCacheEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase(){
    abstract fun StockCacheDao():StockCacheDao
    abstract fun WatchlistDao():WatchlistDao
    abstract fun companyOverviewDao(): CompanyOverviewDao // <<<< NEW DAO METHOD
    abstract fun historicalDataDao(): HistoricalDataDao
}