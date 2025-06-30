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
import androidx.navigation.fragment.findNavController

import com.example.tracstock.domain.model.Stock
import androidx.lifecycle.repeatOnLifecycle
import com.example.tracstock.R

import com.example.tracstock.util.showSnackbar
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import com.example.tracstock.domain.model.Watchlist

@AndroidEntryPoint
class AddWatchlistDialogFragment : DialogFragment() {

    private var _binding: DialogAddWatchlistBinding? = null
    private val binding get() = _binding!!

    private val viewModel: WatchlistViewModel by viewModels()
    private val args : AddWatchlistDialogFragmentArgs by navArgs()

    private var selectedWatchlistId: Long? = null
    private var selectedWatchlistName: String? = null
    private var allWatchlists: List<Watchlist> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogAddWatchlistBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.dialogTitle.text = if (args.isAddToWatchlist) {
            getString(R.string.add_to_watchlist_dialog_title)
        } else {
            getString(R.string.dialog_create_watchlist_title)
        }

        if (args.isAddToWatchlist) {
            binding.newWatchlistSection.visibility = View.VISIBLE
            binding.orTextView.visibility = View.VISIBLE
            binding.existingWatchlistSection.visibility = View.VISIBLE
        } else {
            binding.newWatchlistSection.visibility = View.VISIBLE
            binding.orTextView.visibility = View.GONE
            binding.existingWatchlistSection.visibility = View.GONE
        }

        setupClickListeners()
        observeViewModel()
    }

    private fun setupClickListeners() {
        binding.createWatchlistButton.setOnClickListener {
            val newWatchlistName = binding.newWatchlistNameEditText.text.toString().trim()
            viewModel.onCreateWatchlist(newWatchlistName)
        }

        binding.addToExistingWatchlistButton.setOnClickListener {
            if (selectedWatchlistId != null && args.stockSymbol != null) {
                val stockToAdd = Stock(
                    symbol = args.stockSymbol!!,
                    name = args.stockName ?: args.stockSymbol!!,
                    price = args.stockPrice ?: "0.0",
                    currency = args.stockCurrency ?: "USD"
                )
                viewModel.onAddStockToWatchlist(selectedWatchlistId!!, stockToAdd)
            } else {
                binding.root.showSnackbar("Please select a watchlist and ensure stock details are available.")
            }
        }
    }

    private fun observeViewModel() {

        if (args.isAddToWatchlist) {
            viewLifecycleOwner.lifecycleScope.launch {
                repeatOnLifecycle(Lifecycle.State.STARTED) {
                    viewModel.watchlists.collectLatest { watchlists ->
                        allWatchlists = watchlists
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


                            val watchlistNames = watchlists.map { it.name }
                            val adapter = ArrayAdapter(
                                requireContext(),
                                android.R.layout.simple_dropdown_item_1line,
                                watchlistNames
                            )
                            binding.existingWatchlistAutoCompleteTextView.setAdapter(adapter)


                            binding.existingWatchlistAutoCompleteTextView.setOnItemClickListener { parent, _, position, _ ->
                                val selectedName = parent.getItemAtPosition(position).toString()
                                val selectedWatchlist = watchlists.find { it.name == selectedName }
                                selectedWatchlistId = selectedWatchlist?.id
                                selectedWatchlistName = selectedWatchlist?.name
                            }


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



        viewModel.eventFlow.observe(viewLifecycleOwner) { event ->
            when (event) {
                is WatchlistViewModel.WatchlistEvent.WatchlistCreated -> {
                    binding.root.showSnackbar(getString(R.string.watchlist_created_success, binding.newWatchlistNameEditText.text.toString().trim()))
                    if (args.isAddToWatchlist) {
                        val newWatchlistPosition = allWatchlists.indexOfFirst { it.id == event.watchlistId }
                        if (newWatchlistPosition != -1) {
                            val newWatchlistName = allWatchlists[newWatchlistPosition].name
                            binding.existingWatchlistAutoCompleteTextView.setText(newWatchlistName, false)
                            selectedWatchlistId = event.watchlistId
                            selectedWatchlistName = newWatchlistName
                        }
                    } else {
                        dismiss()
                    }
                }
                is WatchlistViewModel.WatchlistEvent.StockAddedToWatchlist -> {
                    binding.root.showSnackbar(getString(R.string.stock_added_to_watchlist_success, event.stockSymbol))
                    findNavController().previousBackStackEntry?.savedStateHandle?.set("stockAddedToWatchlist", true)
                    dismiss()
                }
                is WatchlistViewModel.WatchlistEvent.ShowMessage -> {
                    binding.root.showSnackbar(event.message)
                }
                is WatchlistViewModel.WatchlistEvent.WatchlistDeleted -> {
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
