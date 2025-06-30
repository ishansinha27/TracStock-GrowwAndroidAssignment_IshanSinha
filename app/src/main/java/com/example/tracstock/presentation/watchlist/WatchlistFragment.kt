package com.example.tracstock.presentation.watchlist

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.NavHostFragment.Companion.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.tracstock.R
import com.example.tracstock.databinding.FragmentWatchlistBinding
import com.example.tracstock.domain.model.Watchlist
import com.example.tracstock.presentation.watchlist.WatchlistAdapter
import com.example.tracstock.presentation.watchlist.WatchlistFragmentDirections
import com.example.tracstock.presentation.watchlist.WatchlistViewModel
import com.example.tracstock.util.showSnackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class WatchlistFragment : Fragment() {

    private var _binding: FragmentWatchlistBinding? = null
    private val binding get() = _binding!!

    private val viewModel: WatchlistViewModel by viewModels()

    private lateinit var watchlistAdapter: WatchlistAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWatchlistBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupClickListeners()
        observeViewModel()


    }


    private fun setupRecyclerView() {
        watchlistAdapter = WatchlistAdapter(
            onItemClick = { watchlist ->

                val action = WatchlistFragmentDirections.actionWatchlistFragmentToWatchlistDetailFragment(
                    watchlistId = watchlist.id,
                    watchlistName = watchlist.name
                )
                findNavController().navigate(action)
            },
            onDeleteClick = { watchlist ->
                // Show confirmation dialog before deleting a watchlist
                showDeleteConfirmationDialog(watchlist)
            }
        )
        binding.watchlistsRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = watchlistAdapter
        }
    }

    /**
     * Sets up click listeners for the FAB and empty state button.
     */
    private fun setupClickListeners() {
        binding.addWatchlistFab.setOnClickListener {
            // Navigate to AddWatchlistDialogFragment (for creating a new watchlist)
            val action = WatchlistFragmentDirections.actionWatchlistFragmentToAddWatchlistDialogFragment(
                isAddToWatchlist = false // Indicate that this dialog is for creating a new watchlist
            )
            findNavController().navigate(action)
        }

        binding.createWatchlistEmptyButton.setOnClickListener {
            // Same action as FAB, for empty state
            val action = WatchlistFragmentDirections.actionWatchlistFragmentToAddWatchlistDialogFragment(
                isAddToWatchlist = false
            )
            findNavController().navigate(action)
        }
    }

    /**
     * Observes LiveData and StateFlow from the ViewModel and updates the UI.
     */
    private fun observeViewModel() {






        // Observe the list of watchlists
        val launch = viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.watchlists.collectLatest { watchlists ->
                    watchlistAdapter.submitList(watchlists)
                    // Show/hide empty state based on whether there are watchlists
                    binding.emptyStateView.visibility =
                        if (watchlists.isEmpty()) View.VISIBLE else View.GONE
                    binding.watchlistsRecyclerView.visibility =
                        if (watchlists.isEmpty()) View.GONE else View.VISIBLE
                }
            }
        }

        // Observe one-time events for messages/status updates
        viewModel.eventFlow.observe(viewLifecycleOwner) { event ->
            when (event) {
                is WatchlistViewModel.WatchlistEvent.WatchlistCreated -> {
                    binding.root.showSnackbar(getString(R.string.watchlist_created_success, "Watchlist")) // Generic name for dialog
                }
                is WatchlistViewModel.WatchlistEvent.WatchlistDeleted -> {
                    binding.root.showSnackbar(getString(R.string.watchlist_deleted_success))
                }
                is WatchlistViewModel.WatchlistEvent.ShowMessage -> {
                    binding.root.showSnackbar(event.message)
                }
                is WatchlistViewModel.WatchlistEvent.StockAddedToWatchlist -> {

                    binding.root.showSnackbar(getString(R.string.stock_added_to_watchlist_success, event.stockSymbol))


                }
            }
        }
    }

    /**
     * Displays a confirmation dialog before deleting a watchlist.
     */
    private fun showDeleteConfirmationDialog(watchlist: Watchlist) {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.confirm_delete_watchlist_title))
            .setMessage(getString(R.string.confirm_delete_watchlist_message, watchlist.name))
            .setPositiveButton(R.string.dialog_positive_button_text) { dialog, _ ->
                viewModel.onDeleteWatchlist(watchlist.id)
                dialog.dismiss()
            }
            .setNegativeButton(R.string.dialog_negative_button_text) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}