package com.example.tracstock.domain.usecase.watchlist

import com.example.tracstock.domain.repository.WatchlistRepository
import com.example.tracstock.util.Resource
import javax.inject.Inject

class DeleteWatchlist @Inject constructor(
    private val repository: WatchlistRepository
) {
    suspend operator fun invoke(watchlistId: Long): Resource<Unit> {
        return repository.deleteWatchlist(watchlistId)
    }
}