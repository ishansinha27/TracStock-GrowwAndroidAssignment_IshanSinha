package com.example.tracstock.domain.usecase.watchlist

import com.example.tracstock.domain.repository.WatchlistRepository
import com.example.tracstock.util.Resource
import javax.inject.Inject

class CreateWatchlist @Inject constructor(
    private val repository: WatchlistRepository
) {
    suspend operator fun invoke(name: String): Resource<Long> {
        return repository.createWatchlist(name)
    }
}