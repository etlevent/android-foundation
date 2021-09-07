@file:JvmName("NumUtils")

package ext.android.foundation.extensions

import kotlin.math.pow

/**
 * byte array convert to int
 * @param bytes source array
 * @param isBigEndian
 *  like. 0x12345678
 *  when isBigEndian is True. bytes will be
 *  byte[0] = 0x12 --(high bit)高位
 *  byte[1] = 0x34
 *  byte[2] = 0x56
 *  byte[3] = 0x78 --(low bit)低位
 *  when isBigEndian is False. bytes will be
 *  byte[3] = 0x12 --(high bit)高位
 *  byte[2] = 0x34
 *  byte[1] = 0x56
 *  byte[0] = 0x78 --(low bit)低位
 * @param signed
 */
fun bytes2int(bytes: ByteArray, isBigEndian: Boolean, signed: Boolean): Int {
    var value = 0
    val length: Int = bytes.size
    for (i in 0 until length) {
        val shift: Int = if (isBigEndian) (length - 1 - i) * 8 else i * 8
        value += bytes[i].toInt() and 0xFF shl shift
    }
    return if (signed) {
        if (value < 2.0.pow(length * 8 - 1)) {
            value
        } else {
            (value - 2.0.pow(length * 8)).toInt()
        }
    } else {
        value
    }
}

fun ByteArray.toInt(isBigEndian: Boolean = true) = bytes2int(this, isBigEndian, true)

fun ByteArray.toUInt(isBigEndian: Boolean = true) = bytes2int(this, isBigEndian, false)