package com.maksimowiczm.foodyou.scale.domain

import com.maksimowiczm.foodyou.common.domain.measurement.MeasurementType

enum class ScaleUnit {
    Grams,
    Ounces,
    Milliliters,
    Pounds;

    fun toMeasurementType(): MeasurementType = when (this) {
        Grams -> MeasurementType.Gram
        Ounces -> MeasurementType.Ounce
        Milliliters -> MeasurementType.Milliliter
        Pounds -> MeasurementType.Ounce
    }

    fun toDisplayOunces(rawValue: Double): Double = when (this) {
        Pounds -> rawValue * 16.0
        else -> rawValue
    }
}
