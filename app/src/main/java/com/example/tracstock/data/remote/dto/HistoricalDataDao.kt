package com.example.tracstock.data.remote.dto

import com.example.tracstock.domain.model.HistoricalPrice
import com.google.gson.annotations.SerializedName

data class HistoricalDataDto(
    @SerializedName("Meta Data") val metaData: MetaDataDto?,
    @SerializedName("Time Series (Daily)") val timeSeriesDaily: Map<String, DailyAdjustedDataDto>?
)

data class MetaDataDto(
    @SerializedName("1. Information") val information: String?,
    @SerializedName("2. Symbol") val symbol: String?,
    @SerializedName("3. Last Refreshed") val lastRefreshed: String?,
    @SerializedName("4. Output Size") val outputSize: String?,
    @SerializedName("5. Time Zone") val timeZone: String?
)

data class DailyAdjustedDataDto(
    @SerializedName("1. open") val open: String?,
    @SerializedName("2. high") val high: String?,
    @SerializedName("3. low") val low: String?,
    @SerializedName("4. close") val close: String?,
    @SerializedName("5. adjusted close") val adjustedClose: String?,
    @SerializedName("6. volume") val volume: String?,
    @SerializedName("7. dividend amount") val dividendAmount: String?,
    @SerializedName("8. split coefficient") val splitCoefficient: String?
)
fun DailyAdjustedDataDto.toHistoricalPrice(date: String) : HistoricalPrice{
    return HistoricalPrice(
        date = date,
        open = open ?: "0.0",
        high = high ?: "0.0",
        low = low ?: "0.0",
        close = close ?: "0.0",
        adjustedClose = adjustedClose ?: "0.0",
        volume = volume ?: "0",
        currency = ""
    )
}