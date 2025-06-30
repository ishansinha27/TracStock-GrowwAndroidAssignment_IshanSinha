package com.example.tracstock.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DateUtils {

    fun formatChartDate(dateString: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val outputFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            val date: Date? = inputFormat.parse(dateString)
            if (date != null) {
                outputFormat.format(date)
            } else {
                dateString // Return original if parsing fails
            }
        } catch (e: Exception) {
            e.printStackTrace()
            dateString // Return original on error
        }
    }

    fun dateStringToTimestamp(dateString: String): Long {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            inputFormat.parse(dateString)?.time ?: -1L
        } catch (e: Exception) {
            e.printStackTrace()
            -1L // Return -1 on error
        }
    }
}