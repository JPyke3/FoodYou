package com.maksimowiczm.foodyou.scale.domain

data class ScaleReading(
    val weightGrams: Double,
    val unit: ScaleUnit,
    val isStable: Boolean,
)
