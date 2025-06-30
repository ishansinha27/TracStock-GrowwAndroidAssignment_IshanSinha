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

            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {

                    onItemClick(getItem(position))
                }
            }
        }

        fun bind(stock: Stock) {
            binding.apply {
                stockLogoImageView.setStockPlaceholder()
                stockSymbolTextView.text = stock.symbol
                stockNameTextView.text = stock.name
                stockPriceTextView.text = "${stock.price} ${stock.currency}"

                val changeText = StringBuilder()
                if (!stock.changeAmount.isNullOrBlank()) {
                    changeText.append(stock.changeAmount)
                }
                if (!stock.changePercentage.isNullOrBlank()) {
                    if (changeText.isNotEmpty()) changeText.append(" ")
                    changeText.append("(${stock.changePercentage})")
                }
                stockChangeTextView.text = changeText.toString()
                stock.changeAmount?.toFloatOrNull()?.let { change ->
                    stockChangeTextView.setTextColor(
                        if (change >= 0) Color.GREEN else Color.RED
                    )
                } ?: run {
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

            return oldItem.symbol == newItem.symbol
        }

        override fun areContentsTheSame(oldItem: Stock, newItem: Stock): Boolean {
            return oldItem == newItem
        }
    }

}