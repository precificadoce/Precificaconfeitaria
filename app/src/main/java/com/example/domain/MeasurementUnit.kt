package com.example.domain

enum class MeasurementUnit(
    val symbol: String,
    val label: String,
    val family: UnitFamily,
    val toBaseFactor: Double
) {
    GRAMS("g", "Gramas (g)", UnitFamily.WEIGHT, 1.0),
    KILOGRAMS("kg", "Quilos (kg)", UnitFamily.WEIGHT, 1000.0),
    MILLILITERS("ml", "Mililitros (ml)", UnitFamily.VOLUME, 1.0),
    LITERS("L", "Litros (L)", UnitFamily.VOLUME, 1000.0),
    UNITS("un", "Unidades (un)", UnitFamily.COUNT, 1.0);

    enum class UnitFamily {
        WEIGHT, VOLUME, COUNT
    }

    fun isCompatibleWith(other: MeasurementUnit): Boolean = this.family == other.family

    fun compatibleUnits(): List<MeasurementUnit> = entries.filter { it.family == this.family }

    companion object {
        fun fromSymbol(symbol: String): MeasurementUnit =
            entries.firstOrNull { it.symbol.equals(symbol, ignoreCase = true) } ?: UNITS
    }
}
