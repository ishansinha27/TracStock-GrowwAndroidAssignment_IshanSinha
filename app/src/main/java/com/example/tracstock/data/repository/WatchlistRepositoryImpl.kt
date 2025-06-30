package com.example.tracstock.data.repository

import com.example.tracstock.data.local.dao.WatchlistDao
import com.example.tracstock.data.local.entities.WatchlistEntity
import com.example.tracstock.domain.model.Watchlist
import com.example.tracstock.data.local.entities.toWatchlist
import com.example.tracstock.data.local.entities.toWatchlistItem
import com.example.tracstock.data.local.entities.toWatchlistItemEntity
import com.example.tracstock.domain.model.Stock
import com.example.tracstock.domain.model.WatchlistItem
import com.example.tracstock.domain.repository.WatchlistRepository
import com.example.tracstock.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WatchlistRepositoryImpl @Inject constructor(
    private val watchlistDao: WatchlistDao
) : WatchlistRepository {

    override suspend fun createWatchlist(name: String): Resource<Long> {
        return try {
            val newWatchlistId = watchlistDao.insertWatchlist(WatchlistEntity(name = name))
            Resource.Success(newWatchlistId)
        } catch (e: Exception) {
            // Check for unique constraint violation if you add one later
            Resource.Error("Failed to create watchlist: ${e.message}")
        }
    }

    override fun getWatchlists(): Flow<List<Watchlist>> {
        // Map flow of WatchlistEntity to flow of Watchlist domain model
        return watchlistDao.getAllWatchlists().map { entities ->
            entities.map { it.toWatchlist() }
        }
    }

    override suspend fun addStockToWatchlist(watchlistId: Long, stock: Stock): Resource<Unit> {
        return try {
            // Use the extension function on Stock to create the entity
            val watchlistItemEntity = stock.toWatchlistItemEntity(watchlistId)
            watchlistDao.insertWatchlistItem(watchlistItemEntity)
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error("Failed to add stock to watchlist: ${e.message}")
        }
    }

    override suspend fun removeStockFromWatchlist(watchlistId: Long, stock: Stock): Resource<Unit> {
        return try {
            // Create a WatchlistItemEntity that matches the one to be deleted
            val watchlistItemEntity = com.example.tracstock.data.local.entities.WatchlistItemEntity(
                watchlistId = watchlistId,
                stockSymbol = stock.symbol,
                stockName = stock.name, // These might not be used for deletion, but for completeness
                stockPrice = stock.price,
                stockCurrency = stock.currency
            )
            val deletedRows = watchlistDao.deleteWatchlistItem(watchlistItemEntity)
            if (deletedRows > 0) {
                Resource.Success(Unit)
            } else {
                Resource.Error("Stock not found in watchlist.")
            }
        } catch (e: Exception) {
            Resource.Error("Failed to remove stock from watchlist: ${e.message}")
        }
    }

    override suspend fun deleteWatchlist(watchlistId: Long): Resource<Unit> {
        return try {
            val deletedRows = watchlistDao.deleteWatchlistById(watchlistId)
            if (deletedRows > 0) {
                Resource.Success(Unit)
            } else {
                Resource.Error("Watchlist not found.")
            }
        } catch (e: Exception) {
            Resource.Error("Failed to delete watchlist: ${e.message}")
        }
    }

    override fun getStocksInWatchlist(watchlistId: Long): Flow<List<WatchlistItem>> {
        // Map flow of WatchlistItemEntity to flow of WatchlistItem domain model
        return watchlistDao.getStocksInWatchlist(watchlistId).map { entities ->
            entities.map { it.toWatchlistItem() }
        }
    }

    override suspend fun isStockInAnyWatchlist(symbol: String): Boolean {
        return watchlistDao.isStockInAnyWatchlist(symbol)
    }
}