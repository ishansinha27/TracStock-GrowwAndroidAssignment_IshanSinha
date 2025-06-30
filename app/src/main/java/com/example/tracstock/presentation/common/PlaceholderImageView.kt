package com.example.tracstock.presentation.common

import android.widget.ImageView
import com.bumptech.glide.Glide
import com.example.tracstock.R
import com.example.tracstock.databinding.ItemStockBinding

fun ImageView.setStockPlaceholder() {


    Glide.with(context)
        .load(R.drawable.ic_stock_placeholder)
        .circleCrop()
        .into(this)
}