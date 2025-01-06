package com.seed.crypto

private const val HEX_CHARS_STR = "0123456789abcdef"
private val HEX_CHARS = HEX_CHARS_STR.toCharArray()

fun ByteArray.toHex(): String = joinToString("") { it.toUByte().formatted() }

fun UByte.formatted(): String = toString(16).padStart(2, '0')

fun String.hexStringToByteArray(): ByteArray {
    val result = ByteArray(length / 2)

    for (i in indices step 2) {
        val firstIndex = HEX_CHARS_STR.indexOf(this[i])
        val secondIndex = HEX_CHARS_STR.indexOf(this[i + 1])

        val octet = firstIndex.shl(4).or(secondIndex)
        result[i.shr(1)] = octet.toByte()
    }

    return result
}