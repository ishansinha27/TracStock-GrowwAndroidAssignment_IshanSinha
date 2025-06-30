package com.example.tracstock.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.tracstock.data.local.dao.StockCacheDao
import com.example.tracstock.data.local.dao.WatchlistDao
import com.example.tracstock.data.local.entities.StockCacheEntity
import com.example.tracstock.data.local.entities.WatchlistEntity
import com.example.tracstock.data.local.entities.WatchlistItemEntity

@Database(entities = [StockCacheEntity::class, WatchlistEntity::class, WatchlistItemEntity::class],
    version = 1)
abstract class AppDatabase : RoomDatabase(){
    abstract fun StockCacheDao():StockCacheDao
    abstract fun WatchlistDao():WatchlistDao
}