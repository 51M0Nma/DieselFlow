package com.example.ui.components

import java.text.NumberFormat
import java.util.Locale

object FormatUtils {
    fun formatINR(amount: Double): String {
        // Formats to Indian Rupee with comma grouping (e.g. 10,61,900.00)
        return try {
            val formatter = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
            formatter.maximumFractionDigits = 2
            formatter.minimumFractionDigits = 2
            formatter.format(amount)
        } catch (e: Exception) {
            String.format(Locale.US, "₹%,.2f", amount)
        }
    }

    fun formatNumber(amount: Double, decimals: Int = 2): String {
        return try {
            String.format(Locale.US, "%,.${decimals}f", amount)
        } catch (e: Exception) {
            amount.toString()
        }
    }
}
