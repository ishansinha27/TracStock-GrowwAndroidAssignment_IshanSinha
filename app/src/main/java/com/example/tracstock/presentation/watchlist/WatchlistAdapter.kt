package com.example.tracstock.presentation.watchlist

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.tracstock.R
import com.example.tracstock.databinding.ItemWatchlistBinding
import com.example.tracstock.domain.model.Watchlist

class WatchlistAdapter(
    private val onItemClick: (Watchlist) -> Unit,
    private val onDeleteClick: (Watchlist) -> Unit
) : ListAdapter<Watchlist, WatchlistAdapter.WatchlistViewHolder>(WatchlistDiffCallback()) {


    inner class WatchlistViewHolder(private val binding: ItemWatchlistBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            // Set click listener for the entire item view to navigate to watchlist details
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(getItem(position))
                }
            }
            // Set click listener for the delete button
            binding.deleteWatchlistButton.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onDeleteClick(getItem(position))
                }
            }
        }

        fun bind(watchlist: Watchlist) {
            binding.apply {
                watchlistNameTextView.text = watchlist.name

            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WatchlistViewHolder {
        val binding = ItemWatchlistBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return WatchlistViewHolder(binding)
    }

    override fun onBindViewHolder(holder: WatchlistViewHolder, position: Int) {
        val watchlist = getItem(position)
        holder.bind(watchlist)
    }

    /**
     * DiffUtil Callback for [Watchlist] objects.
     */
    class WatchlistDiffCallback : DiffUtil.ItemCallback<Watchlist>() {
        override fun areItemsTheSame(oldItem: Watchlist, newItem: Watchlist): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Watchlist, newItem: Watchlist): Boolean {
            return oldItem == newItem
        }
    }
}