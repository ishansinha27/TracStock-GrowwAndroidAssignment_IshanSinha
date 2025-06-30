package com.example.tracstock.presentation.watchlist

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tracstock.R
import com.example.tracstock.domain.model.Stock
import com.example.tracstock.presentation.common.StockAdapter
import com.example.tracstock.databinding.FragmentWatchlistDetailBinding

import com.example.tracstock.util.showSnackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


@AndroidEntryPoint
class WatchlistDetailFragment : Fragment() {

    private var _binding: FragmentWatchlistDetailBinding? = null
    private val binding get() = _binding!!

    private val viewModel: WatchlistDetailViewModel by viewModels()
    private val args: WatchlistDetailFragmentArgs by navArgs()

    private lateinit var stockAdapter: StockAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWatchlistDetailBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set Toolbar title dynamically to the watchlist name
        (activity as? AppCompatActivity)?.supportActionBar?.title = args.watchlistName

        setupRecyclerView()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        stockAdapter = StockAdapter { stock ->
            // Navigate to ProductDetailFragment when a stock item is clicked
            val action = WatchlistDetailFragmentDirections.actionWatchlistDetailFragmentToProductDetailFragment(stock.symbol)
            findNavController().navigate(action)
        }
        binding.stocksInWatchlistRecyclerView.apply {
            layoutManager = GridLayoutManager(context,2)
            adapter = stockAdapter
        }

        // Setup swipe-to-delete for stocks in watchlist
        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
            0, // No drag directions
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT // Swipe left or right
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false // Not implementing drag and drop
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                // Use currentList to access the item directly from the adapter's backing list
                val stockToRemove = stockAdapter.currentList[position] // Access Stock object directly

                // Show confirmation dialog before actual removal
                showRemoveStockConfirmationDialog(stockToRemove, position)
            }
        }).attachToRecyclerView(binding.stocksInWatchlistRecyclerView)
    }
    private fun observeViewModel() {
        // Observe the list of stocks in the current watchlist
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.stocksInWatchlist.collectLatest { watchlistItems ->
                    // Convert WatchlistItem list to Stock list for StockAdapter
                    val stocks = watchlistItems.map { it.stock }
                    stockAdapter.submitList(stocks)
                    // Show/hide empty state based on whether there are stocks
                    binding.emptyStateStocksView.visibility = if (stocks.isEmpty()) View.VISIBLE else View.GONE
                    binding.stocksInWatchlistRecyclerView.visibility = if (stocks.isEmpty()) View.GONE else View.VISIBLE
                }
            }
        }

        // Observe one-time events for messages/status updates
        viewModel.eventFlow.observe(viewLifecycleOwner) { event ->
            when (event) {
                is WatchlistDetailViewModel.WatchlistDetailEvent.StockRemoved -> {
                    binding.root.showSnackbar(getString(R.string.stock_removed_from_watchlist, event.stockSymbol))
                }
                is WatchlistDetailViewModel.WatchlistDetailEvent.ShowMessage -> {
                    binding.root.showSnackbar(event.message)
                }
            }
        }
    }







    private fun showRemoveStockConfirmationDialog(stock: Stock, adapterPosition: Int) {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.confirm_remove_stock_title))
            .setMessage(getString(R.string.confirm_remove_stock_message, stock.symbol))
            .setPositiveButton(R.string.dialog_positive_button_text) { dialog, _ ->
                viewModel.onRemoveStockFromWatchlist(stock)
                dialog.dismiss()
            }
            .setNegativeButton(R.string.dialog_negative_button_text) { dialog, _ ->
                // If cancelled, re-draw the item to remove the swipe visual effect
                stockAdapter.notifyItemChanged(adapterPosition)
                dialog.dismiss()
            }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}