package com.example.domain

import java.text.NumberFormat
import java.util.Locale
import kotlin.math.max

data class PricedIngredientLine(
    val ingredientId: Long,
    val name: String,
    val quantityUsed: Double,
    val unitUsed: MeasurementUnit,
    val packageQuantity: Double,
    val packageUnit: MeasurementUnit,
    val packagePrice: Double,
    val lineCost: Double
)

data class RecipePricingBreakdown(
    val ingredientsCost: Double,
    val overheadCost: Double,
    val laborCost: Double,
    val packagingCost: Double,
    val totalBatchCost: Double,
    val unitCost: Double,
    val desiredProfit: Double,
    val baseSellingTotal: Double,
    val cardFeeCost: Double,
    val finalSellingTotal: Double,
    val suggestedUnitPrice: Double,
    val profitPerUnit: Double,
    val realMarginOnSalePercent: Double,
    val yieldQuantity: Double,
    val yieldUnit: String
)

object PricingCalculator {
    private val ptBrLocale = Locale.Builder().setLanguage("pt").setRegion("BR").build()

    fun formatBrl(value: Double): String {
        val formatter = NumberFormat.getCurrencyInstance(ptBrLocale)
        return formatter.format(if (value.isNaN() || value.isInfinite()) 0.0 else value)
    }

    fun formatPercent(value: Double): String {
        val safe = if (value.isNaN() || value.isInfinite()) 0.0 else value
        return String.format(ptBrLocale, "%.1f%%", safe)
    }

    fun formatQuantity(value: Double): String {
        val safe = if (value.isNaN() || value.isInfinite()) 0.0 else value
        return if (safe % 1.0 == 0.0) {
            safe.toLong().toString()
        } else {
            String.format(ptBrLocale, "%.2f", safe)
        }
    }

    fun calculateIngredientLineCost(
        quantityUsed: Double,
        unitUsed: MeasurementUnit,
        packageQuantity: Double,
        packageUnit: MeasurementUnit,
        packagePrice: Double
    ): Double {
        if (packageQuantity <= 0.0 || packagePrice <= 0.0 || quantityUsed <= 0.0) return 0.0
        val usedInBase = quantityUsed * unitUsed.toBaseFactor
        val packInBase = packageQuantity * packageUnit.toBaseFactor
        if (packInBase <= 0.0) return 0.0
        return (usedInBase / packInBase) * packagePrice
    }

    fun calculatePackageUnitCostDisplay(
        packageQuantity: Double,
        packageUnit: MeasurementUnit,
        packagePrice: Double
    ): String {
        if (packageQuantity <= 0.0 || packagePrice <= 0.0) return "R$ 0,00 / ${packageUnit.symbol}"
        return when (packageUnit) {
            MeasurementUnit.KILOGRAMS -> {
                val perKg = packagePrice / packageQuantity
                "${formatBrl(perKg)} / kg"
            }
            MeasurementUnit.GRAMS -> {
                val per100g = (packagePrice / packageQuantity) * 100.0
                "${formatBrl(per100g)} / 100g"
            }
            MeasurementUnit.LITERS -> {
                val perLiter = packagePrice / packageQuantity
                "${formatBrl(perLiter)} / L"
            }
            MeasurementUnit.MILLILITERS -> {
                val per100ml = (packagePrice / packageQuantity) * 100.0
                "${formatBrl(per100ml)} / 100ml"
            }
            MeasurementUnit.UNITS -> {
                val perUnit = packagePrice / packageQuantity
                "${formatBrl(perUnit)} / un"
            }
        }
    }

    fun calculateBreakdown(
        ingredientsCost: Double,
        packagingCost: Double,
        prepTimeMinutes: Int,
        hourlyRate: Double,
        overheadPercent: Double,
        profitMarkupPercent: Double,
        cardFeePercent: Double,
        yieldQuantity: Double,
        yieldUnit: String
    ): RecipePricingBreakdown {
        val safeYield = max(1.0, yieldQuantity)
        val safeIngredients = max(0.0, ingredientsCost)
        val safePackaging = max(0.0, packagingCost)
        val safeOverheadPct = overheadPercent.coerceIn(0.0, 200.0)
        val safeMarkupPct = profitMarkupPercent.coerceIn(0.0, 500.0)
        val safeFeePct = cardFeePercent.coerceIn(0.0, 50.0)
        val safeMinutes = max(0, prepTimeMinutes)
        val safeHourly = max(0.0, hourlyRate)

        val overheadCost = safeIngredients * (safeOverheadPct / 100.0)
        val laborCost = (safeMinutes / 60.0) * safeHourly
        val totalBatchCost = safeIngredients + overheadCost + laborCost + safePackaging
        val unitCost = totalBatchCost / safeYield

        val desiredProfit = totalBatchCost * (safeMarkupPct / 100.0)
        val baseSellingTotal = totalBatchCost + desiredProfit
        val finalSellingTotal = if (safeFeePct > 0.0) {
            baseSellingTotal / (1.0 - (safeFeePct / 100.0))
        } else {
            baseSellingTotal
        }
        val cardFeeCost = finalSellingTotal - baseSellingTotal
        val suggestedUnitPrice = finalSellingTotal / safeYield
        val profitPerUnit = desiredProfit / safeYield
        val realMarginOnSalePercent = if (finalSellingTotal > 0.0) {
            (desiredProfit / finalSellingTotal) * 100.0
        } else {
            0.0
        }

        return RecipePricingBreakdown(
            ingredientsCost = safeIngredients,
            overheadCost = overheadCost,
            laborCost = laborCost,
            packagingCost = safePackaging,
            totalBatchCost = totalBatchCost,
            unitCost = unitCost,
            desiredProfit = desiredProfit,
            baseSellingTotal = baseSellingTotal,
            cardFeeCost = cardFeeCost,
            finalSellingTotal = finalSellingTotal,
            suggestedUnitPrice = suggestedUnitPrice,
            profitPerUnit = profitPerUnit,
            realMarginOnSalePercent = realMarginOnSalePercent,
            yieldQuantity = safeYield,
            yieldUnit = yieldUnit.ifBlank { "un" }
        )
    }
}
