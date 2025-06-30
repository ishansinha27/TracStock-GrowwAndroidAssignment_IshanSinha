package com.example.tracstock.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.tracstock.data.local.entities.CompanyOverviewCacheEntity

@Dao
interface CompanyOverviewDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCompanyOverview(entity: CompanyOverviewCacheEntity)

    @Query("SELECT * FROM company_overviews_cache WHERE symbol = :symbol")
    suspend fun getCompanyOverview(symbol: String): CompanyOverviewCacheEntity?
}