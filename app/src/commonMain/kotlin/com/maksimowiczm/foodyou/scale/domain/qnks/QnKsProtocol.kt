package com.maksimowiczm.foodyou.scale.domain.qnks

import com.maksimowiczm.foodyou.scale.domain.ScaleProtocol
import com.maksimowiczm.foodyou.scale.domain.ScaleReading
import com.maksimowiczm.foodyou.scale.domain.ScaleUnit
import java.util.UUID

class QnKsProtocol : ScaleProtocol {
    override val serviceUuid: UUID = UUID.fromString("0000fff0-0000-1000-8000-00805f9b34fb")
    override val notifyCharacteristicUuid: UUID = UUID.fromString("0000fff1-0000-1000-8000-00805f9b34fb")

    override fun matchesDevice(deviceName: String?, serviceUuids: List<UUID>): Boolean {
        return serviceUuids.contains(serviceUuid) || (deviceName != null && deviceName.startsWith("QN-"))
    }

    override fun parseNotification(characteristicUuid: UUID, value: ByteArray): ScaleReading? {
        if (value.size < 11) return null
        if ((value[0].toInt() and 0xFF) != 0x10) return null

        val unit = when (value[7].toInt() and 0xFF) {
            0x01 -> ScaleUnit.Grams
            0x02 -> ScaleUnit.Ounces
            0x04 -> ScaleUnit.Milliliters
            0x08 -> ScaleUnit.Pounds
            else -> return null
        }

        val isStable = (value[8].toInt() and 0xFF) and 0xF0 == 0xF0
        val raw = ((value[9].toInt() and 0xFF) shl 8) or (value[10].toInt() and 0xFF)
        val weight = when (value[11].toInt() and 0xFF) {
            0x7E -> raw / 10.0
            else -> raw.toDouble()
        }

        return ScaleReading(weightGrams = weight, unit = unit, isStable = isStable)
    }
}
