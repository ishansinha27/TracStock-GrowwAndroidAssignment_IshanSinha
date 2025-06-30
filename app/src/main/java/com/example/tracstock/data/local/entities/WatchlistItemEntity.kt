package com.example.tracstock.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import com.example.tracstock.domain.model.Stock
import com.example.tracstock.domain.model.WatchlistItem

@Entity(
    tableName = "watchlist_items",
    primaryKeys = ["watchlistId", "stockSymbol"], // Composite primary key
    foreignKeys = [ForeignKey(
        entity = WatchlistEntity::class,
        parentColumns = ["id"],
        childColumns = ["watchlistId"],
        onDelete = ForeignKey.CASCADE // Delete items if parent watchlist is deleted
    )]
)
data class WatchlistItemEntity(
    val watchlistId: Long,
    val stockSymbol: String,
    val stockName: String,
    val stockPrice: String,
    val stockCurrency: String
)
fun WatchlistItemEntity.toWatchlistItem(): WatchlistItem {
    return WatchlistItem(
        watchlistId = this.watchlistId,
        stock = Stock(
            symbol = this.stockSymbol,
            name = this.stockName,
            price = this.stockPrice,
            currency = this.stockCurrency
        )
    )
}
fun WatchlistItem.toWatchlistItemEntity(watchlistId: Long): WatchlistItemEntity {
    return WatchlistItemEntity(
        watchlistId = watchlistId,
        stockSymbol = this.stock.symbol,
        stockName = this.stock.name,
        stockPrice = this.stock.price,
        stockCurrency = this.stock.currency
    )
}

fun Stock.toWatchlistItemEntity(watchlistId: Long): WatchlistItemEntity {
    return WatchlistItemEntity(
        watchlistId = watchlistId,
        stockSymbol = this.symbol,
        stockName = this.name,
        stockPrice = this.price,
        stockCurrency = this.currency
    )
}