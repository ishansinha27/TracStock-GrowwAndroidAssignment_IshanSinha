package com.example.tracstock.data.repository

import com.example.tracstock.BuildConfig
import com.example.tracstock.data.local.dao.CompanyOverviewDao
import com.example.tracstock.data.local.dao.HistoricalDataDao
import com.example.tracstock.util.Constants
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.example.tracstock.data.local.dao.StockCacheDao
import com.example.tracstock.data.local.entities.CompanyOverviewCacheEntity
import com.example.tracstock.data.local.entities.HistoricalDataCacheEntity
import com.example.tracstock.data.local.entities.StockCacheEntity
import com.example.tracstock.data.local.entities.toCompanyOverview
import com.example.tracstock.data.local.entities.toHistoricalPriceList
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
    val gson: Gson,
    val companyOverviewDao: CompanyOverviewDao,
    val historicalDataDao: HistoricalDataDao
) : StockRepository{

    private val API_KEY =BuildConfig.API_KEY

    private inline fun <Dto, DomainModel> fetchDataAndCache(
        cacheType: String,
        crossinline fetchFromNetwork: suspend (String) -> Dto,
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
                    mapDtoToDomain(parseCacheToDto(cachedData!!.data))))
            }
        } catch (e: HttpException) {
            val errorMessage = e.response()?.errorBody()?.string() ?: "An unexpected error occurred."
            emit(Resource.Error(errorMessage, cachedData?.let {
                mapDtoToDomain(parseCacheToDto(it.data)) } ?: emptyList()))
        } catch (e: Exception) {
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
        val currentTime = System.currentTimeMillis()
        val cachedOverview = companyOverviewDao.getCompanyOverview(symbol)
        val isCacheValid = cachedOverview != null &&
                (currentTime - cachedOverview.timestamp) < Constants.COMPANY_OVERVIEW_CACHE_EXPIRATION_MILLIS

        if (isCacheValid) {
            try {
                val overview = cachedOverview!!.toCompanyOverview(gson)
                return Resource.Success(overview)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return try {
            val dto: CompanyOverviewDto = apiService.getCompanyOverview(symbol = symbol, apiKey = API_KEY)
            if (dto.symbol.isNullOrBlank()) {
                if (cachedOverview != null) {
                    val overview = cachedOverview.toCompanyOverview(gson)
                    Resource.Error("Company overview not found on network. Using expired cached data.", overview)
                } else {
                    Resource.Error("Company overview not found for $symbol")
                }
            } else {
                companyOverviewDao.insertCompanyOverview(
                    CompanyOverviewCacheEntity(
                        symbol = dto.symbol!!,
                        data = gson.toJson(dto),
                        timestamp = currentTime
                    )
                )
                Resource.Success(dto.toCompanyOverview())
            }
        } catch (e: IOException) {
            if (cachedOverview != null) {
                val overview = cachedOverview.toCompanyOverview(gson)
                Resource.Error("Couldn't reach server. Showing cached data (expired).", overview)
            } else {
                Resource.Error("Couldn't reach server. Check your internet connection.")
            }
        } catch (e: HttpException) {
            val errorMessage = e.response()?.errorBody()?.string() ?: "An unexpected API error occurred."
            if (cachedOverview != null) {
                val overview = cachedOverview.toCompanyOverview(gson)
                Resource.Error("Error ${e.code()}: $errorMessage. Showing cached data (expired).", overview)
            } else {
                Resource.Error("Error ${e.code()}: $errorMessage")
            }
        } catch (e: Exception) {
            val errorMessage = e.message ?: "An unknown error occurred."
            if (cachedOverview != null) {
                val overview = cachedOverview.toCompanyOverview(gson)
                Resource.Error("An unknown error occurred: $errorMessage. Showing cached data (expired).", overview)
            } else {
                Resource.Error("An unknown error occurred: $errorMessage")
            }
        }
    }

    override suspend fun getHistoricalDailyAdjusted(symbol: String): Resource<List<HistoricalPrice>> {
        val currentTime = System.currentTimeMillis()
        val cachedHistoricalData = historicalDataDao.getHistoricalData(symbol)
        val isCacheValid = cachedHistoricalData != null &&
                (currentTime - cachedHistoricalData.timestamp) < Constants.HISTORICAL_DATA_CACHE_EXPIRATION_MILLIS

        val companyOverviewRes = getCompanyOverview(symbol)
        val currency = (companyOverviewRes.data?.currency ?: "USD")
        if (isCacheValid) {
            try {
                val historicalPrices = cachedHistoricalData!!.toHistoricalPriceList(gson, currency)
                return Resource.Success(historicalPrices)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        return try {
            val dto: IntradayDataDto = apiService.getHistoricalDailyAdjusted(symbol = symbol, apiKey = API_KEY)

            if (dto.timeSeries == null || dto.timeSeries.isEmpty()) {
                if (cachedHistoricalData != null) {
                    val historicalPrices = cachedHistoricalData.toHistoricalPriceList(gson, currency)
                    Resource.Error("Historical intraday data not found on network. Using expired cached data.", historicalPrices)
                } else {
                    Resource.Error("Historical intraday data not found for $symbol")
                }
            } else {
                historicalDataDao.insertHistoricalData(
                    HistoricalDataCacheEntity(
                        symbol = symbol,
                        data = gson.toJson(dto),
                        timestamp = currentTime
                    )
                )
                val historicalPrices = dto.timeSeries.map { (dateTime, dataDto) ->
                    dataDto.toHistoricalPrice(dateTime, currency)
                }.sortedBy { it.date }
                Resource.Success(historicalPrices)
            }
        } catch (e: IOException) {
            if (cachedHistoricalData != null) {
                val historicalPrices = cachedHistoricalData.toHistoricalPriceList(gson, currency)
                Resource.Error("Couldn't reach server. Showing cached data (expired).", historicalPrices)
            } else {
                Resource.Error("Couldn't reach server. Check your internet connection.")
            }
        } catch (e: HttpException) {
            val errorMessage = e.response()?.errorBody()?.string() ?: "An unexpected error occurred."
            if (cachedHistoricalData != null) {
                val historicalPrices = cachedHistoricalData.toHistoricalPriceList(gson, currency)
                Resource.Error("Error ${e.code()}: $errorMessage. Showing cached data (expired).", historicalPrices)
            } else {
                Resource.Error("Error ${e.code()}: $errorMessage")
            }
        } catch (e: Exception) {
            val errorMessage = e.message ?: "An unknown error occurred."
            if (cachedHistoricalData != null) {
                val historicalPrices = cachedHistoricalData.toHistoricalPriceList(gson, currency)
                Resource.Error("An unknown error occurred: $errorMessage. Showing cached data (expired).", historicalPrices)
            } else {
                Resource.Error("An unknown error occurred: $errorMessage")
            }
        }
    }






    override suspend fun searchStocks(keywords: String): Resource<List<Stock>> {
        if (keywords.isBlank()) {
            return Resource.Success(emptyList())
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