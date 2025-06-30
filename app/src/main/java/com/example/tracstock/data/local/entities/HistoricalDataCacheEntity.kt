package com.example.tracstock.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.Gson
import com.example.tracstock.data.remote.dto.IntradayDataDto
import com.example.tracstock.data.remote.dto.toHistoricalPrice // Assuming this extension exists
import com.example.tracstock.domain.model.HistoricalPrice
import java.text.SimpleDateFormat
import java.util.Locale

@Entity(tableName = "historical_data_cache")
data class HistoricalDataCacheEntity(
    @PrimaryKey val symbol: String,
    val data: String,
    val timestamp: Long
)

fun HistoricalDataCacheEntity.toIntradayDataDto(gson: Gson): IntradayDataDto {
    return gson.fromJson(this.data, IntradayDataDto::class.java)
}

fun HistoricalDataCacheEntity.toHistoricalPriceList(gson: Gson, currency: String): List<HistoricalPrice> {
    val intradayDataDto = this.toIntradayDataDto(gson)
    val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    return intradayDataDto.timeSeries?.map { (dateTime, dataDto) ->
        dataDto.toHistoricalPrice(dateTime, currency)
    }?.sortedBy { inputFormat.parse(it.date)?.time ?: 0L } ?: emptyList()
}