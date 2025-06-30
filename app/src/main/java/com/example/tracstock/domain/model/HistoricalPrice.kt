package com.example.tracstock.domain.model

data class HistoricalPrice(
    val date: String,
    val open: String,
    val high: String,
    val low: String,
    val close: String,
    val adjustedClose: String,
    val volume: String,
    val currency: String
)
