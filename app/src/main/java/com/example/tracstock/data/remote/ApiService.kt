package com.example.tracstock.data.remote

import com.example.tracstock.data.remote.dto.CompanyOverviewDto
import com.example.tracstock.data.remote.dto.HistoricalDataDto
import com.example.tracstock.data.remote.dto.IntradayDataDto
import com.example.tracstock.data.remote.dto.StockSearchDto
import com.example.tracstock.data.remote.dto.TopGainersLosersDto
import retrofit2.http.GET
import retrofit2.http.Query


interface ApiService {

    @GET("query")
    suspend fun getTopGainersLosers(
        @Query("function") function: String = "TOP_GAINERS_LOSERS",
        @Query("apikey") apiKey: String
    ): TopGainersLosersDto

    @GET("query")
    suspend fun getCompanyOverview(
        @Query("function") function: String = "OVERVIEW",
        @Query("symbol") symbol: String,
        @Query("apikey") apiKey: String
    ): CompanyOverviewDto

    @GET("query")
    suspend fun getHistoricalDailyAdjusted(
        @Query("function") function: String = "TIME_SERIES_INTRADAY",
        @Query("symbol") symbol: String,
        @Query("interval") interval: String = "60min",
        @Query("extended_hours") extendedHours: String = "false",
        @Query("apikey") apiKey: String
    ): IntradayDataDto


    @GET("query")
    suspend fun searchSymbol(
        @Query("function") function: String = "SYMBOL_SEARCH",
        @Query("keywords") keywords: String,
        @Query("apikey") apiKey: String
    ): StockSearchDto
}