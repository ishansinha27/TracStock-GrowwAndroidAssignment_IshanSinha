package com.example.tracstock.domain.usecase.productdetail

import com.example.tracstock.domain.repository.WatchlistRepository
import javax.inject.Inject

class IsStockInWatchlist @Inject constructor(val repository : WatchlistRepository) {

    suspend operator fun invoke(symbol :String):Boolean{
        return repository.isStockInAnyWatchlist(symbol)
    }
}