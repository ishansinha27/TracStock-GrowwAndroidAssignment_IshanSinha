package com.example.tracstock.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Stock(
    val symbol: String,
    val name: String,
    val price: String,
    val currency: String,
    val changeAmount: String? = null,
    val changePercentage: String? = null,
    val volume: String? = null,
    val type: String? = null,
    val region: String? = null,
    val matchScore: String? = null
) : Parcelable