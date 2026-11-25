package com.example

import com.example.domain.MeasurementUnit
import com.example.domain.PricingCalculator
import org.junit.Assert.assertEquals
import org.junit.Test

class PricingCalculatorTest {
    @Test
    fun `converts kg package to grams used accurately`() {
        val cost = PricingCalculator.calculateIngredientLineCost(
            quantityUsed = 250.0,
            unitUsed = MeasurementUnit.GRAMS,
            packageQuantity = 1.0,
            packageUnit = MeasurementUnit.KILOGRAMS,
            packagePrice = 40.0
        )
        assertEquals(10.0, cost, 0.001)
    }

    @Test
    fun `calculates batch and unit pricing with labor overhead and card fee`() {
        val breakdown = PricingCalculator.calculateBreakdown(
            ingredientsCost = 40.0,
            packagingCost = 10.0,
            prepTimeMinutes = 60,
            hourlyRate = 30.0,
            overheadPercent = 15.0, // +6.0
            profitMarkupPercent = 100.0,
            cardFeePercent = 5.0,
            yieldQuantity = 20.0,
            yieldUnit = "un"
        )
        // totalBatchCost = 40 + 6 + 30 + 10 = 86.0
        assertEquals(86.0, breakdown.totalBatchCost, 0.001)
        assertEquals(4.30, breakdown.unitCost, 0.001)
        // desiredProfit = 86.0 -> baseSelling = 172.0 -> with 5% fee: 172 / 0.95 = 181.0526...
        assertEquals(181.0526, breakdown.finalSellingTotal, 0.01)
        assertEquals(9.0526, breakdown.suggestedUnitPrice, 0.01)
    }
}
