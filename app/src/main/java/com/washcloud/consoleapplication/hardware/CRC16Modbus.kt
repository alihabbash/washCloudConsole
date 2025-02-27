package com.washcloud.consoleapplication.hardware


object CRC16Modbus {
    private const val POLYNOMIAL = 0xA001

    fun compute(data: ByteArray): Int {
        var crc = 0xFFFF
        for (byte in data) {
            crc = crc xor (byte.toInt() and 0xFF)
            for (i in 0 until 8) {
                crc = if ((crc and 1) != 0) {
                    (crc shr 1) xor POLYNOMIAL
                } else {
                    crc shr 1
                }
            }
        }
        return crc
    }

    fun toHex(crc: Int): String {
        // Swap bytes (Modbus CRC is little-endian)
        val swapped = ((crc and 0xFF) shl 8) or ((crc shr 8) and 0xFF)
        return String.format("%04X", swapped)
    }

    fun hexStringToByteArray(hex: String): ByteArray {
        return hex.chunked(2).map { it.toInt(16).toByte() }.toByteArray()
    }
}
