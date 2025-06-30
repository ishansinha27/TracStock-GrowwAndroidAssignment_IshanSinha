package com.example.tracstock.presentation.common

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.tracstock.databinding.ItemStockBinding

import com.example.tracstock.domain.model.Stock

class StockAdapter(private val onItemClick: (Stock) -> Unit) :
    ListAdapter<Stock, StockAdapter.StockViewHolder>(StockDiffCallback()) {

    inner class StockViewHolder(private val binding: ItemStockBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            // Set click listener for the entire item view
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    // Invoke the onItemClick lambda with the clicked Stock object
                    onItemClick(getItem(position))
                }
            }
        }

        fun bind(stock: Stock) {
            binding.apply {
                // Set the static placeholder image
                stockLogoImageView.setStockPlaceholder()

                stockSymbolTextView.text = stock.symbol
                stockNameTextView.text = stock.name
                // Format price with currency
                stockPriceTextView.text = "${stock.price} ${stock.currency}"

                // Handle change amount and percentage (can be null for search results)
                val changeText = StringBuilder()
                if (!stock.changeAmount.isNullOrBlank()) {
                    changeText.append(stock.changeAmount)
                }
                if (!stock.changePercentage.isNullOrBlank()) {
                    if (changeText.isNotEmpty()) changeText.append(" ")
                    changeText.append("(${stock.changePercentage})")
                }
                stockChangeTextView.text = changeText.toString()

                // Set color based on change (green for gain, red for loss)
                stock.changeAmount?.toFloatOrNull()?.let { change ->
                    stockChangeTextView.setTextColor(
                        if (change >= 0) Color.GREEN else Color.RED
                    )
                } ?: run {
                    // Default color if changeAmount is null or cannot be parsed
                    stockChangeTextView.setTextColor(Color.GRAY)
                }
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StockViewHolder {

            val binding = ItemStockBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            return StockViewHolder(binding)
        }


    override fun onBindViewHolder(holder: StockViewHolder, position: Int) {

            val stock = getItem(position)
            holder.bind(stock)

    }
    class StockDiffCallback : DiffUtil.ItemCallback<Stock>() {
        override fun areItemsTheSame(oldItem: Stock, newItem: Stock): Boolean {
            // Items are the same if their unique identifiers (symbols) are the same.
            return oldItem.symbol == newItem.symbol
        }

        override fun areContentsTheSame(oldItem: Stock, newItem: Stock): Boolean {
            // Contents are the same if all relevant data fields are identical.
            // This ensures UI is only updated when necessary.
            return oldItem == newItem
        }
    }

}