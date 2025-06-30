package com.example.tracstock.domain.usecase.productdetail

import com.example.tracstock.domain.model.HistoricalPrice
import com.example.tracstock.domain.repository.StockRepository
import com.example.tracstock.util.Resource
import javax.inject.Inject

class GetHistoricalDailyAdjusted @Inject constructor(val repository: StockRepository) {
    suspend operator fun invoke(symbol : String): Resource<List<HistoricalPrice>> {
        return repository.getHistoricalDailyAdjusted(symbol)
    }
}