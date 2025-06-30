package com.example.tracstock.data.remote.dto

import com.example.tracstock.domain.model.Stock
import com.google.gson.annotations.SerializedName

data class StockSearchDto(
    val bestMatches : List<SymbolMatchDto>?
)
data class SymbolMatchDto(
    @SerializedName("1. symbol") val symbol: String?,
    @SerializedName("2. name") val name: String?,
    @SerializedName("3. type") val type: String?,
    @SerializedName("4. region") val region: String?,
    @SerializedName("5. marketOpen") val marketOpen: String?,
    @SerializedName("6. marketClose") val marketClose: String?,
    @SerializedName("7. timezone") val timezone: String?,
    @SerializedName("8. currency") val currency: String?,
    @SerializedName("9. matchScore") val matchScore: String?
)

fun SymbolMatchDto.toStock() : Stock{
    return Stock(
        symbol = symbol ?: "",
        name = name ?: "",
        price = "0.0",
        currency = currency ?: "",
        type = type,
        region = region,
        matchScore = matchScore
    )
}