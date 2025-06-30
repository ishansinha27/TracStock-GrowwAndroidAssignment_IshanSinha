package com.example.tracstock.presentation.viewall

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.tracstock.R
import com.example.tracstock.databinding.FragmentViewAllBinding
import com.example.tracstock.presentation.common.StockAdapter
import com.example.tracstock.util.Resource
import com.example.tracstock.util.showSnackbar
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class ViewAllFragment : Fragment() {

    private var _binding: FragmentViewAllBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ViewAllViewModel by viewModels()
    private val args: ViewAllFragmentArgs by navArgs() // Safe Args for retrieving the category

    private lateinit var stockAdapter: StockAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentViewAllBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set Toolbar title dynamically based on the category
        val title = when (args.category) {
            "top_gainers" -> getString(R.string.category_top_gainers)
            "top_losers" -> getString(R.string.category_top_losers)
            else -> "All Stocks" // Fallback title
        }
        (activity as? AppCompatActivity)?.supportActionBar?.title = title

        setupRecyclerView()
        observeViewModel()
    }

    /**
     * Initializes the RecyclerView with its adapter and layout manager.
     */
    private fun setupRecyclerView() {
        stockAdapter = StockAdapter { stock ->
            // Navigate to ProductDetailFragment when a stock item is clicked
            val action = ViewAllFragmentDirections.actionViewAllFragmentToProductDetailFragment(stock.symbol)
            findNavController().navigate(action)
        }
        binding.viewAllStocksRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = stockAdapter
        }
    }

    /**
     * Observes LiveData from the ViewModel and updates the UI.
     */
    private fun observeViewModel() {
        viewModel.stocks.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    binding.viewAllProgressBar.visibility = View.VISIBLE
                    binding.viewAllErrorTextView.visibility = View.GONE
                    binding.viewAllStocksRecyclerView.visibility = View.GONE
                }
                is Resource.Success -> {
                    binding.viewAllProgressBar.visibility = View.GONE
                    binding.viewAllErrorTextView.visibility = View.GONE
                    binding.viewAllStocksRecyclerView.visibility = View.VISIBLE
                    stockAdapter.submitList(resource.data)
                    if (resource.data.isNullOrEmpty()) {
                        binding.viewAllErrorTextView.visibility = View.VISIBLE
                        binding.viewAllErrorTextView.text = "No stocks available for this category."
                    }
                }
                is Resource.Error -> {
                    binding.viewAllProgressBar.visibility = View.GONE
                    binding.viewAllStocksRecyclerView.visibility = View.GONE
                    binding.viewAllErrorTextView.visibility = View.VISIBLE
                    binding.viewAllErrorTextView.text = resource.message ?: getString(R.string.error_loading_stocks)
                    binding.root.showSnackbar(resource.message ?: getString(R.string.error_loading_stocks))
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
