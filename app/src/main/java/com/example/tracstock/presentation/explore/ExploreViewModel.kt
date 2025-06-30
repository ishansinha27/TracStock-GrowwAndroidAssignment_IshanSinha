package com.example.tracstock.presentation.explore

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tracstock.domain.model.Stock
import com.example.tracstock.domain.usecase.explore.GetTopGainers
import com.example.tracstock.domain.usecase.explore.GetTopLosers
import com.example.tracstock.domain.usecase.explore.SearchStocks
import com.example.tracstock.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest // For Flow collection
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExploreViewModel @Inject constructor(
    private val getTopGainers: GetTopGainers,
    private val getTopLosers: GetTopLosers,
    private val searchStocks: SearchStocks
) : ViewModel() {

    private val _topGainers = MutableLiveData<Resource<List<Stock>>>()
    val topGainers: LiveData<Resource<List<Stock>>> = _topGainers

    private val _topLosers = MutableLiveData<Resource<List<Stock>>>()
    val topLosers: LiveData<Resource<List<Stock>>> = _topLosers

    private val _searchResults = MutableLiveData<Resource<List<Stock>>>()
    val searchResults: LiveData<Resource<List<Stock>>> = _searchResults

    private var searchJob: Job? = null

    init {
        viewModelScope.launch {
            fetchTopGainers()
            delay(1500L) // Wait 1.5 seconds before fetching the next list
            fetchTopLosers()
        }
    }


    fun fetchTopGainers() {
        viewModelScope.launch {
            getTopGainers().collectLatest { resource ->
                _topGainers.value = resource
            }
        }
    }

    fun fetchTopLosers() {
        viewModelScope.launch {
            getTopLosers().collectLatest { resource ->
                _topLosers.value = resource
            }
        }
    }

    fun searchStocks(query: String) {
        searchJob?.cancel()
        if (query.isBlank()) {
            _searchResults.value = Resource.Success(emptyList())
            return
        }
        searchJob = viewModelScope.launch {
            delay(500L) // Debounce delay: wait for 500ms after user stops typing

            _searchResults.value = Resource.Loading() // Show loading state for search

            val result = searchStocks.invoke(query) // Call the search use case
            _searchResults.value = result // Update LiveData with search result
        }
    }
}

