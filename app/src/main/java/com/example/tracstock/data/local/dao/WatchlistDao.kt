package com.example.tracstock.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.tracstock.data.local.entities.WatchlistEntity
import com.example.tracstock.data.local.entities.WatchlistItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WatchlistDao {

    //watchlist fnc.
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertWatchlist(watchlistEntity: WatchlistEntity):Long

    @Query("SELECT * FROM watchlists ORDER BY name ASC")
    fun getAllWatchlists():Flow<List<WatchlistEntity>>

    @Query("DELETE FROM WATCHLISTS WHERE id=:watchlistId")
    suspend fun deleteWatchlistById(watchlistId:Long):Int

    //watchlistitem fnc....
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWatchlistItem(watchlistItemEntity: WatchlistItemEntity)

    @Delete
    suspend fun deleteWatchlistItem(watchlistItemEntity: WatchlistItemEntity):Int

    @Query("SELECT * FROM watchlist_items WHERE watchlistId = :watchlistId ORDER BY stockName ASC")
    fun getStocksInWatchlist(watchlistId: Long): Flow<List<WatchlistItemEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM watchlist_items WHERE watchlistId = :watchlistId AND stockSymbol = :stockSymbol LIMIT 1)")
    suspend fun isStockInSpecificWatchlist(watchlistId: Long, stockSymbol: String): Boolean

    @Query("SELECT EXISTS(SELECT 1 FROM watchlist_items WHERE stockSymbol = :stockSymbol LIMIT 1)")
    suspend fun isStockInAnyWatchlist(stockSymbol: String): Boolean

    @Update
    suspend fun updateWatchlistItem(item: WatchlistItemEntity)
}