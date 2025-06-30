package com.example.tracstock.di

import com.example.tracstock.data.repository.StockRepositoryImpl
import com.example.tracstock.data.repository.WatchlistRepositoryImpl
import com.example.tracstock.domain.repository.StockRepository
import com.example.tracstock.domain.repository.WatchlistRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class) // Install in SingletonComponent for application-level singletons
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindStockRepository(stockRepositoryImpl: StockRepositoryImpl): StockRepository

    @Binds
    @Singleton
    abstract fun bindWatchlistRepository(watchlistRepositoryImpl: WatchlistRepositoryImpl): WatchlistRepository

}