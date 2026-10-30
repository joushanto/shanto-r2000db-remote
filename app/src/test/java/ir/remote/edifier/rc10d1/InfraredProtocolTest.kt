package com.shanto.r2000dbremote

import org.junit.Assert.*
import org.junit.Test

class InfraredProtocolTest {
    @Test fun framesMatchR2000dbReference() {
        val expected = mapOf(
            0x00 to 0xFF00E710L, 0x01 to 0xFE01E710L,
            0x09 to 0xF609E710L, 0x0C to 0xF30CE710L,
            0x0D to 0xF20DE710L, 0x16 to 0xE916E710L,
            0x0A to 0xF50AE710L, 0x15 to 0xEA15E710L,
            0x0E to 0xF10EE710L, 0x14 to 0xEB14E710L
        )
        for ((command, frame) in expected) {
            assertEquals(frame.toInt(), InfraredProtocol.frame(command))
            val pattern = InfraredProtocol.pattern(command)
            assertEquals(67, pattern.size)
            assertArrayEquals(intArrayOf(9000, 4500), pattern.take(2).toIntArray())
            assertEquals(562, pattern.last())
            // Independently decode transmitted space durations back to the frame.
            var decoded = 0L
            for (bit in 0..31) {
                assertEquals(562, pattern[2 + bit * 2])
                when (pattern[3 + bit * 2]) {
                    1687 -> decoded = decoded or (1L shl bit)
                    562 -> Unit
                    else -> fail("Invalid NEC space")
                }
            }
            assertEquals(frame, decoded)
        }
    }
}
