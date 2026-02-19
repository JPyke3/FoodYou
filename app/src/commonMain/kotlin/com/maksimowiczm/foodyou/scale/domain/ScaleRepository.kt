package com.maksimowiczm.foodyou.scale.domain

import kotlinx.coroutines.flow.StateFlow

interface ScaleRepository {
    val connectionState: StateFlow<ScaleConnectionState>

    fun startScanning()

    fun stopScanning()

    fun disconnect()
}
