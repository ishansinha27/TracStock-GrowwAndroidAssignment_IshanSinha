package com.example.tracstock.presentation.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.DialogFragment
import com.example.tracstock.databinding.DialogAddWatchlistBinding
import com.example.tracstock.presentation.watchlist.WatchlistViewModel
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController // Ensure this import is present!

import com.example.tracstock.domain.model.Stock
import androidx.lifecycle.repeatOnLifecycle
import com.example.tracstock.R

import com.example.tracstock.util.showSnackbar
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import com.example.tracstock.domain.model.Watchlist // Import Watchlist model

@AndroidEntryPoint
class AddWatchlistDialogFragment : DialogFragment() {

    private var _binding: DialogAddWatchlistBinding? = null
    private val binding get() = _binding!!

    private val viewModel: WatchlistViewModel by viewModels() // Use shared ViewModel
    private val args : AddWatchlistDialogFragmentArgs by navArgs()

    private var selectedWatchlistId: Long? = null
    private var selectedWatchlistName: String? = null
    private var allWatchlists: List<Watchlist> = emptyList() // <<<< FIX: Declare allWatchlists here

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogAddWatchlistBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Adjust dialog title based on mode
        binding.dialogTitle.text = if (args.isAddToWatchlist) {
            getString(R.string.add_to_watchlist_dialog_title)
        } else {
            getString(R.string.dialog_create_watchlist_title)
        }

        // Conditionally show/hide sections based on `isAddToWatchlist` argument
        if (args.isAddToWatchlist) {
            binding.newWatchlistSection.visibility = View.VISIBLE
            binding.orTextView.visibility = View.VISIBLE
            binding.existingWatchlistSection.visibility = View.VISIBLE
        } else {
            // Only show new watchlist creation if not adding a specific stock
            binding.newWatchlistSection.visibility = View.VISIBLE
            binding.orTextView.visibility = View.GONE
            binding.existingWatchlistSection.visibility = View.GONE
        }

        setupClickListeners()
        observeViewModel()
    }

    /**
     * Sets up click listeners for buttons in the dialog.
     */
    private fun setupClickListeners() {
        binding.createWatchlistButton.setOnClickListener {
            val newWatchlistName = binding.newWatchlistNameEditText.text.toString().trim()
            viewModel.onCreateWatchlist(newWatchlistName)
        }

        binding.addToExistingWatchlistButton.setOnClickListener {
            if (selectedWatchlistId != null && args.stockSymbol != null) {
                val stockToAdd = Stock(
                    symbol = args.stockSymbol!!,
                    name = args.stockName ?: args.stockSymbol!!, // Fallback to symbol if name is null
                    price = args.stockPrice ?: "0.0",
                    currency = args.stockCurrency ?: "USD"
                )
                viewModel.onAddStockToWatchlist(selectedWatchlistId!!, stockToAdd)
            } else {
                binding.root.showSnackbar("Please select a watchlist and ensure stock details are available.")
            }
        }
    }

    /**
     * Observes LiveData and StateFlow from the ViewModel.
     */
    private fun observeViewModel() {
        // Observe watchlists for the dropdown (only if in add-to-watchlist mode)
        if (args.isAddToWatchlist) {
            viewLifecycleOwner.lifecycleScope.launch {
                repeatOnLifecycle(Lifecycle.State.STARTED) {
                    viewModel.watchlists.collectLatest { watchlists ->
                        allWatchlists = watchlists // <<<< FIX: Assign collected watchlists to allWatchlists
                        if (watchlists.isEmpty()) {
                            delay(2500)
                            binding.existingWatchlistSection.visibility = View.GONE
                            binding.orTextView.visibility = View.GONE
                            binding.dialogTitle.text = getString(R.string.dialog_create_watchlist_title)
                            binding.root.showSnackbar("No watchlists found. Please create a new one.")
                        } else {
                            binding.existingWatchlistSection.visibility = View.VISIBLE
                            binding.orTextView.visibility = View.VISIBLE
                            binding.dialogTitle.text = getString(R.string.add_to_watchlist_dialog_title)

                            // Populate AutoCompleteTextView with watchlist names
                            val watchlistNames = watchlists.map { it.name }
                            val adapter = ArrayAdapter(
                                requireContext(),
                                android.R.layout.simple_dropdown_item_1line,
                                watchlistNames
                            )
                            binding.existingWatchlistAutoCompleteTextView.setAdapter(adapter)

                            // Listen for selection changes in the dropdown
                            binding.existingWatchlistAutoCompleteTextView.setOnItemClickListener { parent, _, position, _ ->
                                val selectedName = parent.getItemAtPosition(position).toString()
                                val selectedWatchlist = watchlists.find { it.name == selectedName }
                                selectedWatchlistId = selectedWatchlist?.id
                                selectedWatchlistName = selectedWatchlist?.name
                            }

                            // Pre-select the first watchlist if none is selected initially
                            if (selectedWatchlistId == null) {
                                val firstWatchlist = watchlists.firstOrNull()
                                if (firstWatchlist != null) {
                                    binding.existingWatchlistAutoCompleteTextView.setText(firstWatchlist.name, false)
                                    selectedWatchlistId = firstWatchlist.id
                                    selectedWatchlistName = firstWatchlist.name
                                }
                            }
                        }
                    }
                }
            }
        }


        // Observe one-time events from the ViewModel
        viewModel.eventFlow.observe(viewLifecycleOwner) { event ->
            when (event) {
                is WatchlistViewModel.WatchlistEvent.WatchlistCreated -> {
                    // Show success message and dismiss dialog
                    binding.root.showSnackbar(getString(R.string.watchlist_created_success, binding.newWatchlistNameEditText.text.toString().trim()))
                    // If stock is being added, automatically select the new watchlist
                    if (args.isAddToWatchlist) {
                        // Find the position of the newly created watchlist and select it
                        // <<<< FIX: Use AutoCompleteTextView instead of Spinner, and update selection logic >>>>
                        val newWatchlistPosition = allWatchlists.indexOfFirst { it.id == event.watchlistId }
                        if (newWatchlistPosition != -1) {
                            val newWatchlistName = allWatchlists[newWatchlistPosition].name
                            binding.existingWatchlistAutoCompleteTextView.setText(newWatchlistName, false)
                            selectedWatchlistId = event.watchlistId
                            selectedWatchlistName = newWatchlistName
                        }
                    } else {
                        dismiss() // Dismiss if only creating a watchlist
                    }
                }
                is WatchlistViewModel.WatchlistEvent.StockAddedToWatchlist -> {
                    binding.root.showSnackbar(getString(R.string.stock_added_to_watchlist_success, event.stockSymbol))
                    // <<<< CRITICAL FIX: Set result for ProductDetailFragment >>>>
                    findNavController().previousBackStackEntry?.savedStateHandle?.set("stockAddedToWatchlist", true)
                    dismiss() // Dismiss dialog on successful add
                }
                is WatchlistViewModel.WatchlistEvent.ShowMessage -> {
                    binding.root.showSnackbar(event.message)
                }
                is WatchlistViewModel.WatchlistEvent.WatchlistDeleted -> {
                    // This event is primarily for WatchlistFragment; dialog doesn't delete watchlists itself.
                    // If it somehow gets here, ignore or log.
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
