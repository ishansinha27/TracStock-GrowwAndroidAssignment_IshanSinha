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

    private val binding get() = _binding!!

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
        searchResultsAdapter = SearchStockAdapter { stock ->
            val action = ExploreFragmentDirections.actionExploreFragmentToProductDetailFragment(stock.symbol)
            findNavController().navigate(action)
            binding.searchEditText.setText("")
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

        viewModel.topGainers.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    binding.progressBarGainers.visibility = View.VISIBLE
                    binding.errorTextGainers.visibility = View.GONE
                    binding.rvTopGainers.visibility = View.GONE
                }
                is Resource.Success -> {
                    binding.progressBarGainers.visibility = View.GONE
                    binding.errorTextGainers.visibility = View.GONE
                    binding.rvTopGainers.visibility = View.VISIBLE
                    topGainersAdapter.submitList(resource.data?.take(6))
                }
                is Resource.Error -> {
                    binding.progressBarGainers.visibility = View.GONE
                    if (!resource.data.isNullOrEmpty()) {
                        binding.rvTopGainers.visibility = View.VISIBLE
                        topGainersAdapter.submitList(resource.data.take(6))
                        binding.errorTextGainers.visibility = View.VISIBLE
                        binding.errorTextGainers.text = resource.message ?: getString(R.string.error_loading_data)
                    } else {

                        binding.rvTopGainers.visibility = View.GONE
                        binding.errorTextGainers.visibility = View.VISIBLE
                        binding.errorTextGainers.text = resource.message ?: getString(R.string.error_loading_data)
                    }
                    binding.root.showSnackbar(resource.message ?: getString(R.string.error_loading_data))
                }
            }
        }

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
                        binding.rvTopGainers.visibility = View.VISIBLE
                        topGainersAdapter.submitList(resource.data.take(6))
                        binding.errorTextGainers.visibility = View.VISIBLE
                        binding.errorTextGainers.text = resource.message ?: getString(R.string.error_loading_data)
                    } else {
                        binding.rvTopGainers.visibility = View.GONE
                        binding.errorTextGainers.visibility = View.VISIBLE
                        binding.errorTextGainers.text = resource.message ?: getString(R.string.error_loading_data)
                    }
                    binding.root.showSnackbar(resource.message ?: getString(R.string.error_loading_data))
                }
            }
        }

        viewModel.searchResults.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    if (binding.searchEditText.text?.isNotBlank() == true) {
                        binding.searchResultsRecyclerView.visibility = View.VISIBLE
                        binding.exploreScrollView.visibility = View.GONE

                    }
                }
                is Resource.Success -> {

                    val results = resource.data
                    if (binding.searchEditText.text?.isNotBlank() == true) {
                        if (results.isNullOrEmpty()) {

                            binding.searchResultsRecyclerView.visibility = View.GONE
                            binding.exploreScrollView.visibility = View.VISIBLE
                            binding.root.showSnackbar(getString(R.string.no_search_results))
                        } else {
                            searchResultsAdapter.submitList(results)
                            binding.searchResultsRecyclerView.visibility = View.VISIBLE
                            binding.exploreScrollView.visibility = View.GONE
                        }
                    } else {

                        binding.searchResultsRecyclerView.visibility = View.GONE
                        binding.exploreScrollView.visibility = View.VISIBLE
                    }
                }
                is Resource.Error -> {
                    binding.searchResultsRecyclerView.visibility = View.GONE
                    binding.exploreScrollView.visibility = View.VISIBLE
                    binding.root.showSnackbar(resource.message ?: getString(R.string.error_loading_data))
                }
            }
        }
    }


    private fun setupClickListeners() {
        binding.btnViewAllGainers.setOnClickListener {
            val action = ExploreFragmentDirections.actionExploreFragmentToViewAllFragment("top_gainers")
            findNavController().navigate(action)
        }

        binding.btnViewAllLosers.setOnClickListener {
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