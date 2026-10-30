package com.shanto.r2000dbremote

/** NEC extended-address frames for Edifier R2000DB / RC10D. */
internal object InfraredProtocol {
    fun frame(command: Int): Int {
        require(command in 0..255)
        return ((command xor 0xFF) shl 24) or (command shl 16) or 0xE710
    }

    fun pattern(command: Int): IntArray {
        val data = frame(command)
        var result = intArrayOf(9000, 4500)
        for (i in 0..31) {
            result += if (data and (1 shl i) == 0) intArrayOf(562, 562)
                      else intArrayOf(562, 1687)
        }
        return result + 562
    }
}
