package com.example.tracstock.presentation.explore

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.tracstock.databinding.ItemSearchStockBinding
import com.example.tracstock.domain.model.Stock

class SearchStockAdapter(
    private val onItemClick: (Stock) -> Unit
) : ListAdapter<Stock, SearchStockAdapter.SearchStockViewHolder>(SearchStockDiffCallback()) {

    inner class SearchStockViewHolder(private val binding: ItemSearchStockBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            // Set click listener for the entire item view
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(getItem(position)) // getItem() is protected in ListAdapter, but accessible here.
                }
            }
        }

        fun bind(stock: Stock) {
            binding.apply {
                searchStockTickerTextView.text = stock.symbol
                searchCompanyNameTextView.text = stock.name
                // Note: The search endpoint typically does not return current price,
                // so we don't display it here as per previous discussion.
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchStockViewHolder {
        val binding = ItemSearchStockBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return SearchStockViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SearchStockViewHolder, position: Int) {
        val stock = getItem(position)
        holder.bind(stock)
    }

    /**
     * DiffUtil Callback for [Stock] objects, used for efficient RecyclerView updates.
     */
    class SearchStockDiffCallback : DiffUtil.ItemCallback<Stock>() {
        override fun areItemsTheSame(oldItem: Stock, newItem: Stock): Boolean {
            return oldItem.symbol == newItem.symbol // Stocks are the same if their symbols are identical
        }

        override fun areContentsTheSame(oldItem: Stock, newItem: Stock): Boolean {
            return oldItem == newItem // Data contents are the same
        }
    }
}