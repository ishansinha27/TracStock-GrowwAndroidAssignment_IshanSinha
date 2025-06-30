package com.example.tracstock.domain.usecase.watchlist

import com.example.tracstock.domain.model.Watchlist
import com.example.tracstock.domain.repository.WatchlistRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetWatchlists @Inject constructor(
    private val repository: WatchlistRepository
) {
    operator fun invoke(): Flow<List<Watchlist>> {
        return repository.getWatchlists()
    }
}