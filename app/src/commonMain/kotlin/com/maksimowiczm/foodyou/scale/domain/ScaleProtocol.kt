package com.maksimowiczm.foodyou.scale.domain

import java.util.UUID

interface ScaleProtocol {
    val serviceUuid: UUID
    val notifyCharacteristicUuid: UUID

    fun matchesDevice(deviceName: String?, serviceUuids: List<UUID>): Boolean

    fun parseNotification(characteristicUuid: UUID, value: ByteArray): ScaleReading?
}
