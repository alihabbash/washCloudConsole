package com.washcloud.consoleapplication.hardware


object CRC16Modbus {
    private const val POLYNOMIAL = 0xA001

    fun compute(data: ByteArray): Int {
        var crc = 0xFFFF
        for (b in data) {
            crc = crc xor (b.toInt() and 0xFF)
            for (i in 0..7) {
                crc = if ((crc and 1) != 0) {
                    crc shr 1 xor POLYNOMIAL
                } else {
                    crc shr 1
                }
            }
        }
        return crc
    }

    fun toHex(crc: Int): String {
        // Swap bytes (little-endian format)
        val swapped = ((crc and 0xFF) shl 8) or ((crc shr 8) and 0xFF)
        return String.format("%04X", swapped)
    }

    fun hexStringToByteArray(hex: String): ByteArray {
        val len = hex.length
        val data = ByteArray(len / 2)
        var i = 0
        while (i < len) {
            data[i / 2] = ((hex[i].digitToIntOrNull(16) ?: -1 shl 4)
            + hex[i + 1].digitToIntOrNull(16)!! ?: -1).toByte()
            i += 2
        }
        return data
    }
}