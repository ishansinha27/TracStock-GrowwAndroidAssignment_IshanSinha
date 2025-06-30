package com.example.tracstock.domain.repository

import com.example.tracstock.domain.model.CompanyOverview
import com.example.tracstock.domain.model.HistoricalPrice
import com.example.tracstock.domain.model.Stock
import com.example.tracstock.util.Resource
import kotlinx.coroutines.flow.Flow

interface StockRepository {
    fun getTopGainers(): Flow<Resource<List<Stock>>>
    fun getTopLosers(): Flow<Resource<List<Stock>>>
    suspend fun getCompanyOverview(symbol: String): Resource<CompanyOverview>
    suspend fun getHistoricalDailyAdjusted(symbol: String): Resource<List<HistoricalPrice>>
    suspend fun searchStocks(keywords: String): Resource<List<Stock>>

}