package com.example.tracstock.domain.usecase.explore

import com.example.tracstock.domain.model.Stock
import com.example.tracstock.domain.repository.StockRepository
import com.example.tracstock.util.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetTopLosers @Inject constructor(val repository: StockRepository) {
    operator fun invoke(): Flow<Resource<List<Stock>>>{
        return repository.getTopLosers()
    }
}