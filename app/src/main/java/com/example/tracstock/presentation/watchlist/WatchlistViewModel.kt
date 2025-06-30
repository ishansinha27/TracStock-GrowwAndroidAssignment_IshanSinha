package com.example.tracstock.presentation.watchlist

import android.os.Parcel
import android.os.Parcelable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tracstock.domain.model.Stock
import com.example.tracstock.domain.model.Watchlist
import com.example.tracstock.domain.usecase.watchlist.AddStockToWatchlist
import com.example.tracstock.domain.usecase.watchlist.CreateWatchlist
import com.example.tracstock.domain.usecase.watchlist.DeleteWatchlist
import com.example.tracstock.domain.usecase.watchlist.GetWatchlists
import com.example.tracstock.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WatchlistViewModel @Inject constructor(
    private val getWatchlists: GetWatchlists,
    private val createWatchlist: CreateWatchlist,
    private val deleteWatchlist: DeleteWatchlist,
    private val addStockToWatchlist: AddStockToWatchlist
) : ViewModel() {

    // Expose all watchlists as a StateFlow for real-time updates
    val watchlists: StateFlow<List<Watchlist>> = getWatchlists().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000), // Start collecting when there are subscribers, stop after 5s
        initialValue = emptyList() // Initial empty list
    )

    // LiveData for displaying single-shot UI events (e.g., success/error messages)
    private val _eventFlow = MutableLiveData<WatchlistEvent>()
    val eventFlow: LiveData<WatchlistEvent> = _eventFlow


    fun onCreateWatchlist(name: String) {
        viewModelScope.launch {
            if (name.isBlank()) {
                _eventFlow.value = WatchlistEvent.ShowMessage("Watchlist name cannot be empty.")
                return@launch
            }

            // Check if a watchlist with this name already exists
            if (watchlists.value.any { it.name.equals(name, ignoreCase = true) }) {
                _eventFlow.value =
                    WatchlistEvent.ShowMessage("Watchlist with this name already exists.")
                return@launch
            }

            when (val result = createWatchlist(name)) {
                is Resource.Success -> {
                    _eventFlow.value = WatchlistEvent.WatchlistCreated(result.data!!)
                }

                is Resource.Error -> {
                    _eventFlow.value = WatchlistEvent.ShowMessage(
                        result.message ?: "Unknown error creating watchlist."
                    )
                }

                is Resource.Loading -> { /* Do nothing for loading in a single-shot event */
                }
            }
        }
    }


    fun onAddStockToWatchlist(watchlistId: Long, stock: Stock) {
        viewModelScope.launch {
            when (val result = addStockToWatchlist(watchlistId, stock)) {
                is Resource.Success -> {
                    _eventFlow.value = WatchlistEvent.StockAddedToWatchlist(stock.symbol)
                }

                is Resource.Error -> {
                    _eventFlow.value = WatchlistEvent.ShowMessage(
                        result.message ?: "Failed to add stock to watchlist."
                    )
                }

                is Resource.Loading -> { /* Do nothing for loading */
                }
            }
        }
    }

    fun onDeleteWatchlist(watchlistId: Long) {
        viewModelScope.launch {
            when (val result = deleteWatchlist(watchlistId)) {
                is Resource.Success -> {
                    _eventFlow.value = WatchlistEvent.WatchlistDeleted
                }

                is Resource.Error -> {
                    _eventFlow.value =
                        WatchlistEvent.ShowMessage(result.message ?: "Failed to delete watchlist.")
                }

                is Resource.Loading -> { /* Do nothing for loading */
                }
            }
        }
    }

    sealed class WatchlistEvent {
        data class WatchlistCreated(val watchlistId: Long) : WatchlistEvent()
        data class StockAddedToWatchlist(val stockSymbol: String) : WatchlistEvent()
        object WatchlistDeleted : WatchlistEvent()
        data class ShowMessage(val message: String) : WatchlistEvent()
    }
}