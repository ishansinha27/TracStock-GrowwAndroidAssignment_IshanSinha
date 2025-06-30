package com.example.tracstock.data.repository

import com.example.tracstock.BuildConfig
import com.example.tracstock.util.Constants
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.example.tracstock.data.local.dao.StockCacheDao
import com.example.tracstock.data.local.entities.StockCacheEntity
import com.example.tracstock.data.local.entities.toJsonString
import com.example.tracstock.data.remote.ApiService
import com.example.tracstock.data.remote.dto.CompanyOverviewDto
import com.example.tracstock.data.remote.dto.HistoricalDataDto
import com.example.tracstock.data.remote.dto.IntradayDataDto
import com.example.tracstock.data.remote.dto.StockItemDto
import com.example.tracstock.data.remote.dto.TopGainersLosersDto
import com.example.tracstock.data.remote.dto.toCompanyOverview
import com.example.tracstock.data.remote.dto.toHistoricalPrice
import com.example.tracstock.data.remote.dto.toStock
import com.example.tracstock.domain.model.CompanyOverview
import com.example.tracstock.domain.model.HistoricalPrice
import com.example.tracstock.domain.model.Stock
import com.example.tracstock.domain.repository.StockRepository
import com.example.tracstock.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StockRepositoryImpl @Inject constructor(
    val apiService: ApiService,
    val cacheDao: StockCacheDao,
    val gson: Gson
) : StockRepository{

    private val API_KEY =BuildConfig.API_KEY

    private inline fun <Dto, DomainModel> fetchDataAndCache(
        cacheType: String,
        crossinline fetchFromNetwork: suspend (String) -> Dto, // Added API key param
        crossinline mapDtoToDomain: (Dto) -> List<DomainModel>,
        crossinline mapDtoToCache: (Dto) -> StockCacheEntity,
        crossinline parseCacheToDto: (String) -> Dto
    ): Flow<Resource<List<DomainModel>>> = flow {
        emit(Resource.Loading())

        val cachedData = cacheDao.getCachedData(cacheType)
        val currentTime = System.currentTimeMillis()
        val isCacheValid = cachedData != null &&
                (currentTime - (cachedData.timestamp)) <  Constants.CACHE_EXPIRATION_TIME_MILLIS
        var emittedInitialData=false

        if (cachedData != null) {

            try {
                val parsedDto = parseCacheToDto(cachedData.data)
                val domainModels=mapDtoToDomain(parsedDto)

                if(isCacheValid){
                    emit(Resource.Success(domainModels))
                }else{
                    emit(Resource.Success(domainModels,"Using cached data (expired). Attempting refresh..."))
                }
                emittedInitialData=true
            } catch (e: Exception) {
                emit(Resource.Error("Failed to parse cached data: ${e.message}", emptyList()))
            }
        }

        try {
            val networkResult = fetchFromNetwork(API_KEY)
            cacheDao.insertCache(mapDtoToCache(networkResult))
            emit(Resource.Success(mapDtoToDomain(networkResult)))
        } catch (e: IOException) {
            if (!emittedInitialData) {
                emit(Resource.Error("Couldn't reach server. Check your internet connection.", emptyList()))
            } else {
                emit(Resource.Error("Offline mode. Showing cached data. Connect to internet for real-time details.",
                    mapDtoToDomain(parseCacheToDto(cachedData!!.data)))) // Pass the previously emitted cached data again if needed by UI
            }
        } catch (e: HttpException) {
            // HTTP error (4xx, 5xx)
            val errorMessage = e.response()?.errorBody()?.string() ?: "An unexpected error occurred."
            emit(Resource.Error(errorMessage, cachedData?.let {
                mapDtoToDomain(parseCacheToDto(it.data)) } ?: emptyList()))
        } catch (e: Exception) {
            // Other unexpected errors
            emit(Resource.Error("An unknown error occurred: ${e.message}", cachedData?.let { mapDtoToDomain(parseCacheToDto(it.data)) } ?: emptyList()))
        }
    }

    override fun getTopGainers(): Flow<Resource<List<Stock>>> {
        return fetchDataAndCache(
            cacheType = "top_gainers",
            fetchFromNetwork = { apiKey -> apiService.getTopGainersLosers(apiKey = apiKey) },
            mapDtoToDomain = { dto -> dto.topGainers?.map { it.toStock() } ?: emptyList() },
            mapDtoToCache = { dto -> StockCacheEntity("top_gainers", dto.topGainers!!.toJsonString()?:"", System.currentTimeMillis()) },
            parseCacheToDto = { json -> TopGainersLosersDto(null, null, gson.fromJson(json, object : TypeToken<List<StockItemDto>>() {}.type), null) }
        )
    }

    override fun getTopLosers(): Flow<Resource<List<Stock>>> {
        return fetchDataAndCache(
            cacheType = "top_losers",
            fetchFromNetwork = { apiKey -> apiService.getTopGainersLosers(apiKey = apiKey) },
            mapDtoToDomain = { dto -> dto.topLosers?.map { it.toStock() } ?: emptyList() },
            mapDtoToCache = { dto -> StockCacheEntity("top_losers", dto.topLosers?.toJsonString() ?: "[]", System.currentTimeMillis()) },
            parseCacheToDto = { json -> TopGainersLosersDto(null, null, null, gson.fromJson(json, object : TypeToken<List<StockItemDto>>() {}.type)) }
        )
    }
    override suspend fun getCompanyOverview(symbol: String): Resource<CompanyOverview> {
        return try {
            val dto: CompanyOverviewDto = apiService.getCompanyOverview(symbol = symbol, apiKey = API_KEY)
            if (dto.symbol.isNullOrBlank()) { // Alpha Vantage returns empty object on no data
                Resource.Error("Company overview not found for $symbol")
            } else {
                Resource.Success(dto.toCompanyOverview())
            }
        } catch (e: IOException) {
            Resource.Error("Couldn't reach server. Check your internet connection.")
        } catch (e: HttpException) {
            val errorMessage = e.response()?.errorBody()?.string() ?: "An unexpected error occurred."
            Resource.Error("Error ${e.code()}: $errorMessage")
        } catch (e: Exception) {
            Resource.Error("An unknown error occurred: ${e.message}")
        }
    }

    override suspend fun getHistoricalDailyAdjusted(symbol: String): Resource<List<HistoricalPrice>> {
        return try {
            // No need to pass interval or extendedHours here, as they are now defaulted in the API service.
            val dto: IntradayDataDto = apiService.getHistoricalDailyAdjusted(symbol = symbol, apiKey = API_KEY)

            if (dto.timeSeries == null || dto.timeSeries.isEmpty()) {
                Resource.Error("Historical intraday data not found for $symbol")
            } else {
                val companyOverviewRes = getCompanyOverview(symbol)
                val currency = if (companyOverviewRes is Resource.Success) {
                    companyOverviewRes.data?.currency ?: "USD"
                } else {
                    "USD"
                }

                val historicalPrices = dto.timeSeries.map { (dateTime, dataDto) ->
                    dataDto.toHistoricalPrice(dateTime, currency)
                }.sortedBy { it.date }
                Resource.Success(historicalPrices)
            }
        } catch (e: IOException) {
            Resource.Error("Couldn't reach server. Check your internet connection.")
        } catch (e: HttpException) {
            val errorMessage = e.response()?.errorBody()?.string() ?: "An unexpected error occurred."
            Resource.Error("Error ${e.code()}: $errorMessage")
        } catch (e: Exception) {
            Resource.Error("An unknown error occurred: ${e.message}")
        }
    }






    override suspend fun searchStocks(keywords: String): Resource<List<Stock>> {
        if (keywords.isBlank()) {
            return Resource.Success(emptyList()) // Return empty list for blank queries
        }
        return try {
            val dto = apiService.searchSymbol(keywords = keywords, apiKey = API_KEY)
            val stocks = dto.bestMatches?.map { it.toStock() } ?: emptyList()
            Resource.Success(stocks)
        } catch (e: IOException) {
            Resource.Error("Couldn't reach server for search. Check your internet connection.")
        } catch (e: HttpException) {
            val errorMessage = e.response()?.errorBody()?.string() ?: "An unexpected search error occurred."
            Resource.Error("Error ${e.code()}: $errorMessage")
        } catch (e: Exception) {
            Resource.Error("An unknown search error occurred: ${e.message}")
        }
    }




}