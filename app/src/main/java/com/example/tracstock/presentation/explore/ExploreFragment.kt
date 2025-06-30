package com.example.tracstock.presentation.explore

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.NavHostFragment.Companion.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager

import com.example.tracstock.R
import com.example.tracstock.databinding.FragmentExploreBinding
import com.example.tracstock.presentation.common.StockAdapter
import com.example.tracstock.util.Resource
import com.example.tracstock.util.showSnackbar
import com.google.android.material.internal.ViewUtils.hideKeyboard
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class ExploreFragment : Fragment() {

    private var _binding: FragmentExploreBinding? = null
    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    // Inject ViewModel using by viewModels() delegate
    private val viewModel: ExploreViewModel by viewModels()

    private lateinit var topGainersAdapter: StockAdapter
    private lateinit var topLosersAdapter: StockAdapter
    private lateinit var searchResultsAdapter: SearchStockAdapter


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentExploreBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerViews()
        setupSearch()
        observeViewModel()
        setupClickListeners()
    }
    private fun setupRecyclerViews(){

        topGainersAdapter=StockAdapter{stock->
            val action=ExploreFragmentDirections.actionExploreFragmentToProductDetailFragment(stock.symbol)
            findNavController().navigate(action)
        }
        binding.rvTopGainers.apply {
            layoutManager=GridLayoutManager(context,2)
            adapter=topGainersAdapter

        }
        topLosersAdapter=StockAdapter{stock->
            val action = ExploreFragmentDirections.actionExploreFragmentToProductDetailFragment(stock.symbol)
            findNavController().navigate(action)
        }
        binding.rvTopLosers.apply {
            layoutManager = GridLayoutManager(context,2)
            adapter = topLosersAdapter
        }
        searchResultsAdapter = SearchStockAdapter { stock -> // <<<< Use SearchStockAdapter
            // Navigate to ProductDetailFragment when a search result is clicked
            val action = ExploreFragmentDirections.actionExploreFragmentToProductDetailFragment(stock.symbol)
            findNavController().navigate(action)
            binding.searchEditText.setText("") // Clear search query
            binding.searchEditText.clearFocus()

        }
        binding.searchResultsRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = searchResultsAdapter
        }
    }
    private fun setupSearch(){
        binding.searchEditText.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.searchStocks(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })
        binding.searchEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                binding.searchEditText.clearFocus()
                return@setOnEditorActionListener true
            }
            false
        }
        binding.searchEditText.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus && binding.searchEditText.text.isNullOrBlank()) {
                binding.searchResultsRecyclerView.visibility = View.GONE
                binding.exploreScrollView.visibility = View.VISIBLE
            }
        }
    }
    private fun observeViewModel() {
        // Observe Top Gainers
        viewModel.topGainers.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    binding.progressBarGainers.visibility = View.VISIBLE
                    binding.errorTextGainers.visibility = View.GONE
                    binding.rvTopGainers.visibility = View.GONE // Hide content during loading
                }
                is Resource.Success -> {
                    binding.progressBarGainers.visibility = View.GONE
                    binding.errorTextGainers.visibility = View.GONE
                    binding.rvTopGainers.visibility = View.VISIBLE
                    // Submit only a subset (e.g., first 5-6) for the horizontal list
                    topGainersAdapter.submitList(resource.data?.take(6))
                }
                is Resource.Error -> {
                    binding.progressBarGainers.visibility = View.GONE
                    if (!resource.data.isNullOrEmpty()) {
                        // Show cached data
                        binding.rvTopGainers.visibility = View.VISIBLE
                        topGainersAdapter.submitList(resource.data.take(6))
                        binding.errorTextGainers.visibility = View.VISIBLE
                        binding.errorTextGainers.text = resource.message ?: getString(R.string.error_loading_data)
                    } else {
                        // No data at all, show only error
                        binding.rvTopGainers.visibility = View.GONE
                        binding.errorTextGainers.visibility = View.VISIBLE
                        binding.errorTextGainers.text = resource.message ?: getString(R.string.error_loading_data)
                    }
                    binding.root.showSnackbar(resource.message ?: getString(R.string.error_loading_data))
                }
            }
        }

        // Observe Top Losers
        viewModel.topLosers.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    binding.progressBarLosers.visibility = View.VISIBLE
                    binding.errorTextLosers.visibility = View.GONE
                    binding.rvTopLosers.visibility = View.GONE
                }
                is Resource.Success -> {
                    binding.progressBarLosers.visibility = View.GONE
                    binding.errorTextLosers.visibility = View.GONE
                    binding.rvTopLosers.visibility = View.VISIBLE
                    topLosersAdapter.submitList(resource.data?.take(6))
                }
                is Resource.Error -> {
                    binding.progressBarLosers.visibility = View.GONE
                    if (!resource.data.isNullOrEmpty()) {
                        // Show cached data
                        binding.rvTopGainers.visibility = View.VISIBLE
                        topGainersAdapter.submitList(resource.data.take(6))
                        binding.errorTextGainers.visibility = View.VISIBLE
                        binding.errorTextGainers.text = resource.message ?: getString(R.string.error_loading_data)
                    } else {
                        // No data at all, show only error
                        binding.rvTopGainers.visibility = View.GONE
                        binding.errorTextGainers.visibility = View.VISIBLE
                        binding.errorTextGainers.text = resource.message ?: getString(R.string.error_loading_data)
                    }
                    binding.root.showSnackbar(resource.message ?: getString(R.string.error_loading_data))
                }
            }
        }

        // Observe Search Results
        viewModel.searchResults.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    // Show a progress bar potentially overlaying search results if they are visible
                    // For now, we'll just handle visibility
                    if (binding.searchEditText.text?.isNotBlank() == true) {
                        binding.searchResultsRecyclerView.visibility = View.VISIBLE
                        binding.exploreScrollView.visibility = View.GONE
                        // You could add a specific loading spinner for search results here
                    }
                }
                is Resource.Success -> {
                    // Hide any search specific loading spinner
                    val results = resource.data
                    if (binding.searchEditText.text?.isNotBlank() == true) {
                        if (results.isNullOrEmpty()) {
                            // Show "No results" message
                            binding.searchResultsRecyclerView.visibility = View.GONE
                            binding.exploreScrollView.visibility = View.VISIBLE // Show main content again
                            binding.root.showSnackbar(getString(R.string.no_search_results))
                        } else {
                            searchResultsAdapter.submitList(results)
                            binding.searchResultsRecyclerView.visibility = View.VISIBLE
                            binding.exploreScrollView.visibility = View.GONE // Hide main content
                        }
                    } else {
                        // Query is blank, hide results and show main content
                        binding.searchResultsRecyclerView.visibility = View.GONE
                        binding.exploreScrollView.visibility = View.VISIBLE
                    }
                }
                is Resource.Error -> {
                    // Hide any search specific loading spinner
                    binding.searchResultsRecyclerView.visibility = View.GONE
                    binding.exploreScrollView.visibility = View.VISIBLE // Show main content
                    binding.root.showSnackbar(resource.message ?: getString(R.string.error_loading_data))
                }
            }
        }
    }

    /**
     * Sets up click listeners for "View All" buttons.
     */
    private fun setupClickListeners() {
        binding.btnViewAllGainers.setOnClickListener {
            // Navigate to ViewAllFragment, passing the category "top_gainers"
            val action = ExploreFragmentDirections.actionExploreFragmentToViewAllFragment("top_gainers")
            findNavController().navigate(action)
        }

        binding.btnViewAllLosers.setOnClickListener {
            // Navigate to ViewAllFragment, passing the category "top_losers"
            val action = ExploreFragmentDirections.actionExploreFragmentToViewAllFragment("top_losers")
            findNavController().navigate(action)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }






    companion object {

    }
}