package com.example.tracstock.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.tracstock.data.remote.dto.StockItemDto
import com.example.tracstock.data.remote.dto.toStock
import com.example.tracstock.domain.model.Stock
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

@Entity(tableName = "stock_cache")
data class StockCacheEntity(
    @PrimaryKey val type :String,
    val data : String,
    val timestamp : Long
)

fun List<StockItemDto>.toJsonString(): String {
    return Gson().toJson(this)
}
fun StockCacheEntity.toStocks(): List<Stock> {
    val listType = object : TypeToken<List<StockItemDto>>() {}.type
    val stockItemDtos: List<StockItemDto> = Gson().fromJson(this.data, listType)
    return stockItemDtos.map { it.toStock() }
}