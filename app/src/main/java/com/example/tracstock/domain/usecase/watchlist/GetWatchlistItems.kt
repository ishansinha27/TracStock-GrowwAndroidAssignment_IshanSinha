package com.example.tracstock.domain.usecase.watchlist

import com.example.tracstock.domain.model.WatchlistItem
import com.example.tracstock.domain.repository.WatchlistRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetWatchlistItems @Inject constructor(
    private val repository: WatchlistRepository
) {
    operator fun invoke(watchlistId: Long): Flow<List<WatchlistItem>> {
        return repository.getStocksInWatchlist(watchlistId)
    }
}