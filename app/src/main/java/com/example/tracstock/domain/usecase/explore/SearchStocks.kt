package com.example.tracstock.domain.usecase.explore

import com.example.tracstock.domain.model.Stock
import com.example.tracstock.domain.repository.StockRepository
import com.example.tracstock.util.Resource
import javax.inject.Inject

class SearchStocks @Inject constructor(val repository: StockRepository) {
    suspend operator fun invoke(keywords : String): Resource<List<Stock>>{
        return repository.searchStocks(keywords)
    }
}