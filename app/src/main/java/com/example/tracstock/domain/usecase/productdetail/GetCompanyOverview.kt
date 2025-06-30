package com.example.tracstock.domain.usecase.productdetail

import com.example.tracstock.domain.model.CompanyOverview
import com.example.tracstock.domain.repository.StockRepository
import com.example.tracstock.util.Resource
import javax.inject.Inject

class GetCompanyOverview @Inject constructor(val repository: StockRepository){
    suspend operator fun invoke(symbol : String): Resource<CompanyOverview> {
        return repository.getCompanyOverview(symbol)
    }
}