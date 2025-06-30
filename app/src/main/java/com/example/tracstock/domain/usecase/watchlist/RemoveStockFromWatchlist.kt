package com.example.tracstock.domain.usecase.watchlist

import com.example.tracstock.domain.model.Stock
import com.example.tracstock.domain.repository.WatchlistRepository
import com.example.tracstock.util.Resource
import javax.inject.Inject

class RemoveStockFromWatchlist @Inject constructor(
    private val repository: WatchlistRepository
) {
    suspend operator fun invoke(watchlistId: Long, stock: Stock): Resource<Unit> {
        return repository.removeStockFromWatchlist(watchlistId, stock)
    }
}