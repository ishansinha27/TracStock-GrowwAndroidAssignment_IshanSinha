package com.example.tracstock.data.remote.dto

import com.example.tracstock.domain.model.Stock
import com.google.gson.annotations.SerializedName


data class TopGainersLosersDto(
    @SerializedName("metadata") val metadata: String?,
    @SerializedName("lastUpdated") val lastUpdated: String?,
    @SerializedName("top_gainers") val topGainers: List<StockItemDto>?,
    @SerializedName("top_losers") val topLosers: List<StockItemDto>?,

)

data class StockItemDto(
    @SerializedName("ticker") val ticker: String?,
    @SerializedName("price") val price: String?,
    @SerializedName("change_amount") val changeAmount: String?,
    @SerializedName("change_percentage") val changePercentage: String?,
    @SerializedName("volume") val volume: String?
)
fun StockItemDto.toStock():Stock{
    return Stock(
        symbol = ticker ?: "",
        name = ticker ?: "",
        price = price ?: "",
        currency = "USD",
        changeAmount = changeAmount,
        changePercentage = changePercentage,
        volume = volume
    )
}
