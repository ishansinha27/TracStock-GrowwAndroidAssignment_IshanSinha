package com.example.tracstock.presentation.viewall

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tracstock.domain.model.Stock
import com.example.tracstock.domain.usecase.explore.GetTopGainers
import com.example.tracstock.domain.usecase.explore.GetTopLosers
import com.example.tracstock.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ViewAllViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val getTopGainers: GetTopGainers,
    private val getTopLosers: GetTopLosers
) : ViewModel() {

    // LiveData for the list of stocks to display
    private val _stocks = MutableLiveData<Resource<List<Stock>>>()
    val stocks: LiveData<Resource<List<Stock>>> = _stocks

    // The category of stocks to display (e.g., "top_gainers", "top_losers")
    val category: String = savedStateHandle["category"] ?: ""

    init {
        // Fetch stocks for the given category when the ViewModel is created
        if (category.isNotBlank()) {
            fetchStocksByCategory(category)
        } else {
            _stocks.value = Resource.Error("No category provided.")
        }
    }

    private fun fetchStocksByCategory(category: String) {
        viewModelScope.launch {
            when (category) {
                "top_gainers" -> {
                    getTopGainers().collectLatest { resource ->
                        _stocks.value = resource
                    }
                }
                "top_losers" -> {
                    getTopLosers().collectLatest { resource ->
                        _stocks.value = resource
                    }
                }
                else -> {
                    _stocks.value = Resource.Error("Invalid stock category: $category")
                }
            }
        }
    }
}
