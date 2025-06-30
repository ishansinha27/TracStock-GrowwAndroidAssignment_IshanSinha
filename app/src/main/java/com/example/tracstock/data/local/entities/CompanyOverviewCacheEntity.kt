package com.example.tracstock.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.Gson
import com.example.tracstock.data.remote.dto.CompanyOverviewDto
import com.example.tracstock.domain.model.CompanyOverview
import com.example.tracstock.data.remote.dto.toCompanyOverview

@Entity(tableName = "company_overviews_cache")
data class CompanyOverviewCacheEntity(
    @PrimaryKey val symbol: String,
    val data: String,
    val timestamp: Long
)

fun CompanyOverviewCacheEntity.toCompanyOverviewDto(gson: Gson): CompanyOverviewDto {
    return gson.fromJson(this.data, CompanyOverviewDto::class.java)
}

fun CompanyOverviewCacheEntity.toCompanyOverview(gson: Gson): CompanyOverview {
    return this.toCompanyOverviewDto(gson).toCompanyOverview()
}