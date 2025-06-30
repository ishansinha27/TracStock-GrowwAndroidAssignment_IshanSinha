package com.example.tracstock.presentation.productdetail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tracstock.domain.model.CompanyOverview
import com.example.tracstock.domain.model.HistoricalPrice
import com.example.tracstock.domain.model.Stock
import com.example.tracstock.domain.usecase.productdetail.GetCompanyOverview
import com.example.tracstock.domain.usecase.productdetail.GetHistoricalDailyAdjusted
import com.example.tracstock.domain.usecase.productdetail.IsStockInWatchlist
import com.example.tracstock.domain.usecase.watchlist.GetWatchlistItems
import com.example.tracstock.domain.usecase.watchlist.GetWatchlists
import com.example.tracstock.domain.usecase.watchlist.RemoveStockFromWatchlist
import com.example.tracstock.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProductDetailViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle, // For accessing navigation arguments
    private val getCompanyOverview: GetCompanyOverview,
    private val getHistoricalData: GetHistoricalDailyAdjusted,
    private val isStockInWatchlist: IsStockInWatchlist,
    private val getWatchlists: GetWatchlists,
    private val removeStockFromWatchlist: RemoveStockFromWatchlist,
    private val getWatchlistItems: GetWatchlistItems
) : ViewModel() {

    // LiveData for Company Overview
    private val _companyOverview = MutableLiveData<Resource<CompanyOverview>>()
    val companyOverview: LiveData<Resource<CompanyOverview>> = _companyOverview

    // LiveData for Historical Data (for chart)
    private val _historicalData = MutableLiveData<Resource<List<HistoricalPrice>>>()
    val historicalData: LiveData<Resource<List<HistoricalPrice>>> = _historicalData

    private val _currentPrice = MutableLiveData<String>()
    val currentPrice: LiveData<String> = _currentPrice

    private val _removeResult = MutableLiveData<Resource<Unit>>()
    val removeResult: LiveData<Resource<Unit>> = _removeResult
    // LiveData to track if the current stock is in *any* watchlist
    private val _isStockInAnyWatchlist = MutableLiveData<Boolean>()
    val isStockInAnyWatchlist: LiveData<Boolean> = _isStockInAnyWatchlist

    // The stock symbol received from navigation arguments
    val stockSymbol: String = savedStateHandle["symbol"] ?: ""

    init {
        // Fetch details when ViewModel is created, using the passed symbol
        if (stockSymbol.isNotBlank()) {
            fetchStockDetails(stockSymbol)
        } else {
            _companyOverview.value = Resource.Error("No stock symbol provided.")
            _historicalData.value = Resource.Error("No stock symbol provided.")
            _currentPrice.value = "0.0 USD"
        }
    }


    fun fetchStockDetails(symbol: String) {
        viewModelScope.launch {
            // Fetch Company Overview
            _companyOverview.value = Resource.Loading()
            val overviewResult = getCompanyOverview(symbol)
            _companyOverview.value = overviewResult

            // Fetch Historical Data
            _historicalData.value = Resource.Loading()
            val historicalResult = getHistoricalData(symbol)
            _historicalData.value = historicalResult

            if (historicalResult is Resource.Success && !historicalResult.data.isNullOrEmpty()) {
                val latestPrice = historicalResult.data.last().close // Assuming sorted by date ascending
                val currency = historicalResult.data.last().currency
                _currentPrice.value = "$latestPrice $currency"
            }
            else{
                _currentPrice.value="0.0 USD"
            }

            // Check Watchlist status
            checkWatchlistStatus(symbol)
        }
    }

    fun checkWatchlistStatus(symbol: String) {
        viewModelScope.launch {
            val isInWatchlist = isStockInWatchlist(symbol)
            _isStockInAnyWatchlist.value = isInWatchlist
        }
    }

    fun removeStockFromAnyWatchlist(stock: Stock) {
        viewModelScope.launch {
            val watchlists = getWatchlists().first()
            var found = false
            for (watchlist in watchlists) {
                val items = getWatchlistItems(watchlist.id).first()
                if (items.any { it.stock.symbol == stock.symbol }) {
                    val result = removeStockFromWatchlist(watchlist.id, stock)
                    _removeResult.value = result
                    found = true
                    break
                }
            }
            if (!found) {
                _removeResult.value = Resource.Error("Stock not found in any watchlist.")
            }
            // Refresh status
            checkWatchlistStatus(stock.symbol)
        }
    }




}
