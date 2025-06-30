package com.example.tracstock.di

import com.example.tracstock.data.remote.ApiService
import com.example.tracstock.util.Constants
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.Interceptor
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideGson():Gson{
        return GsonBuilder()
                .setLenient()
                .create()
    }

    @Provides
    @Singleton
    fun provideOkHttpClient():OkHttpClient{
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level= HttpLoggingInterceptor.Level.BODY
        }
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30,TimeUnit.SECONDS)
            .readTimeout(30,TimeUnit.SECONDS)
            .writeTimeout(30,TimeUnit.SECONDS)
            .build()

    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient, gson: Gson): Retrofit{

        return Retrofit.Builder()
            .baseUrl(Constants.ALPHA_VANTAGE_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

    }

    @Provides
    @Singleton
    fun provideAlphaVantageApiService(retrofit: Retrofit): ApiService {
        return retrofit.create(ApiService::class.java)
    }








}