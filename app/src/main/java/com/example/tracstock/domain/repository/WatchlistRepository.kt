package com.example.tracstock.domain.repository

import com.example.tracstock.domain.model.Stock
import com.example.tracstock.domain.model.Watchlist
import com.example.tracstock.domain.model.WatchlistItem
import com.example.tracstock.util.Resource
import kotlinx.coroutines.flow.Flow

interface WatchlistRepository {
    suspend fun createWatchlist(name: String): Resource<Long>
    fun getWatchlists(): Flow<List<Watchlist>>
    suspend fun addStockToWatchlist(watchlistId: Long, stock: Stock): Resource<Unit>
    suspend fun removeStockFromWatchlist(watchlistId: Long, stock: Stock): Resource<Unit>
    suspend fun deleteWatchlist(watchlistId: Long): Resource<Unit>
    fun getStocksInWatchlist(watchlistId: Long): Flow<List<WatchlistItem>>
    suspend fun isStockInAnyWatchlist(symbol: String): Boolean
}