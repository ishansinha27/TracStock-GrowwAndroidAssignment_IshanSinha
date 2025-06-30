package com.example.tracstock.presentation.watchlist

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tracstock.domain.model.Stock
import com.example.tracstock.domain.model.WatchlistItem
import com.example.tracstock.domain.usecase.watchlist.GetWatchlistItems
import com.example.tracstock.domain.usecase.watchlist.RemoveStockFromWatchlist
import com.example.tracstock.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WatchlistDetailViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val getStocksInWatchlist: GetWatchlistItems,
    private val removeStockFromWatchlist: RemoveStockFromWatchlist
) : ViewModel() {

    // Retrieve watchlistId and watchlistName from navigation arguments
    val watchlistId: Long = savedStateHandle["watchlistId"] ?: -1L
    val watchlistName: String = savedStateHandle["watchlistName"] ?: "Watchlist Details"

    // StateFlow for the list of stocks in the current watchlist (real-time updates)
    val stocksInWatchlist: StateFlow<List<WatchlistItem>> = getStocksInWatchlist(watchlistId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000), // Keep collecting for 5s after no active observers
            initialValue = emptyList() // Initial empty list
        )

    // LiveData for one-time UI events (e.g., success/error messages for stock removal)
    private val _eventFlow = MutableLiveData<WatchlistDetailEvent>()
    val eventFlow: LiveData<WatchlistDetailEvent> = _eventFlow

    /**
     * Removes a specific stock from the current watchlist.
     * Posts a [WatchlistDetailEvent] based on the result.
     *
     * @param stock The [Stock] object to remove.
     */
    fun onRemoveStockFromWatchlist(stock: Stock) {
        viewModelScope.launch {
            when (val result = removeStockFromWatchlist(watchlistId, stock)) {
                is Resource.Success -> {
                    _eventFlow.value = WatchlistDetailEvent.StockRemoved(stock.symbol)
                }
                is Resource.Error -> {
                    _eventFlow.value = WatchlistDetailEvent.ShowMessage(result.message ?: "Failed to remove stock.")
                }
                is Resource.Loading -> { /* Do nothing for loading */ }
            }
        }
    }

    /**
     * Sealed class to represent one-time UI events from the ViewModel to the Fragment.
     */
    sealed class WatchlistDetailEvent {
        data class StockRemoved(val stockSymbol: String) : WatchlistDetailEvent()
        data class ShowMessage(val message: String) : WatchlistDetailEvent()
    }
}