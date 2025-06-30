package com.example.tracstock.data.remote.dto

import com.example.tracstock.domain.model.HistoricalPrice
import com.google.gson.annotations.SerializedName

data class IntradayDataDto(
    @SerializedName("Meta Data") val metaData: MetaDataDto?,
    @SerializedName("Time Series (60min)") val timeSeries: Map<String, IntradayTimeSeriesDataDto>?
)
data class IntradayTimeSeriesDataDto(
    @SerializedName("1. open") val open: String?,
    @SerializedName("2. high") val high: String?,
    @SerializedName("3. low") val low: String?,
    @SerializedName("4. close") val close: String?,
    @SerializedName("5. volume") val volume: String?
)
fun IntradayTimeSeriesDataDto.toHistoricalPrice(dateTime: String, currency:String): HistoricalPrice {
    return HistoricalPrice(
        date = dateTime,
        open = open ?: "0.0",
        high = high ?: "0.0",
        low = low ?: "0.0",
        close = close ?: "0.0",
        adjustedClose = close ?: "0.0",
        volume = volume ?: "0",
        currency = currency
    )
}