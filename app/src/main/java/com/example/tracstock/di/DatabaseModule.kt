package com.example.tracstock.di

import android.content.Context
import androidx.room.Room
import com.example.tracstock.data.local.dao.CompanyOverviewDao
import com.example.tracstock.data.local.dao.HistoricalDataDao
import com.example.tracstock.data.local.dao.StockCacheDao
import com.example.tracstock.data.local.dao.WatchlistDao
import com.example.tracstock.data.local.database.AppDatabase
import com.example.tracstock.util.Constants
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase{

        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            Constants.DATABASE_NAME
        ).build()
    }

    @Provides
    @Singleton
    fun provideStockCacheDao(database: AppDatabase): StockCacheDao {
        return database.StockCacheDao()
    }

    @Provides
    @Singleton
    fun provideWatchlistDao(database: AppDatabase): WatchlistDao {
        return database.WatchlistDao()
    }
    @Provides
    @Singleton
    fun provideCompanyOverviewDao(database: AppDatabase): CompanyOverviewDao {
        return database.companyOverviewDao()
    }

    @Provides
    @Singleton
    fun provideHistoricalDataDao(database: AppDatabase): HistoricalDataDao {
        return database.historicalDataDao()
    }
}