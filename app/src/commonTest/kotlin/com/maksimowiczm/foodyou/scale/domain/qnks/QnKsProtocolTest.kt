package com.maksimowiczm.foodyou.scale.domain.qnks

import com.maksimowiczm.foodyou.scale.domain.ScaleUnit
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class QnKsProtocolTest {

    private val protocol = QnKsProtocol()

    private fun packet(
        unit: Byte = 0x01,
        stability: Byte = 0xF0.toByte(),
        weightHigh: Byte = 0x16,
        weightLow: Byte = 0x21,
    ): ByteArray {
        return byteArrayOf(
            0x10,
            0x12,
            0x00,
            0x78.toByte(),
            0x01,
            0x02,
            0x05,
            unit,
            stability,
            weightHigh,
            weightLow,
            0x7E,
            0x1F,
            0x02,
            0x58,
            0x02,
            0x01,
            0x00,
        )
    }

    @Test
    fun testKnownPacketGrams() {
        val result = protocol.parseNotification(
            protocol.notifyCharacteristicUuid,
            packet(unit = 0x01, stability = 0xF0.toByte(), weightHigh = 0x16, weightLow = 0x21),
        )

        assertEquals(566.5, result!!.weightGrams, 0.01)
        assertEquals(ScaleUnit.Grams, result.unit)
        assertTrue(result.isStable)
    }

    @Test
    fun testUnitOunces() {
        val result = protocol.parseNotification(
            protocol.notifyCharacteristicUuid,
            packet(unit = 0x02),
        )

        assertEquals(ScaleUnit.Ounces, result!!.unit)
    }

    @Test
    fun testUnitMilliliters() {
        val result = protocol.parseNotification(
            protocol.notifyCharacteristicUuid,
            packet(unit = 0x04),
        )

        assertEquals(ScaleUnit.Milliliters, result!!.unit)
    }

    @Test
    fun testUnitPounds() {
        val result = protocol.parseNotification(
            protocol.notifyCharacteristicUuid,
            packet(unit = 0x08),
        )

        assertEquals(ScaleUnit.Pounds, result!!.unit)
    }

    @Test
    fun testStabilityUnstable() {
        val result = protocol.parseNotification(
            protocol.notifyCharacteristicUuid,
            packet(stability = 0xE0.toByte()),
        )

        assertFalse(result!!.isStable)
    }

    @Test
    fun testPacketTooShort() {
        val shortPacket = byteArrayOf(0x10, 0x12, 0x00, 0x78.toByte(), 0x01)
        val result = protocol.parseNotification(
            protocol.notifyCharacteristicUuid,
            shortPacket,
        )

        assertNull(result)
    }

    @Test
    fun testWrongHeader() {
        val wrongHeaderPacket = packet()
        wrongHeaderPacket[0] = 0x20
        val result = protocol.parseNotification(
            protocol.notifyCharacteristicUuid,
            wrongHeaderPacket,
        )

        assertNull(result)
    }

    @Test
    fun testUnknownUnit() {
        val result = protocol.parseNotification(
            protocol.notifyCharacteristicUuid,
            packet(unit = 0x10),
        )

        assertNull(result)
    }
}
