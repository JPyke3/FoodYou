package com.maksimowiczm.foodyou.scale.domain

sealed interface ScaleConnectionState {
    data object Idle : ScaleConnectionState
    data object Scanning : ScaleConnectionState
    data object Connecting : ScaleConnectionState
    data class Connected(val reading: ScaleReading?) : ScaleConnectionState
    data object Disconnected : ScaleConnectionState
    data class Error(val message: String) : ScaleConnectionState
}
