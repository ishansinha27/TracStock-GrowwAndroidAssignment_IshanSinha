package com.example.tracstock.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.tracstock.domain.model.Watchlist

@Entity(tableName = "watchlists")
data class WatchlistEntity(
    @PrimaryKey (autoGenerate = true) val id : Long=0,
    val name :String
)

fun WatchlistEntity.toWatchlist():Watchlist{
    return Watchlist(
        id = this.id,
        name = this.name
    )
}

fun Watchlist.toWatchlistEntity(): WatchlistEntity{
    return WatchlistEntity(
        id=this.id,
        name=this.name
    )
}
