package ext.android.foundation.extensions

import java.math.BigInteger
import java.security.MessageDigest

val String.md5: String
    get() {
        val digest = MessageDigest.getInstance("MD5")
        digest.update(toByteArray())
        val bi = BigInteger(1, digest.digest())
        return bi.toString(16).padLeft(32, "0")
    }

val ByteArray.hex: String
    get() = buildString {
        if (this@hex.isEmpty()) return@buildString
        this@hex.forEach {
            val v = it.toInt() and 0xFF
            val h = v.toString(16)
            if (h.length < 2)
                append(0)
            append(h)
        }
    }

fun String.hexToBytes(): ByteArray? {
    if (isEmpty()) return null
    val bytesLen = length / 2
    val bytes = ByteArray(bytesLen)
    (0 until bytesLen).forEach { i ->
        val high = substring(i * 2, i * 2 + 1).toInt(16)
        val low = substring(i * 2 + 1, (i + 1) * 2).toInt(16)
        bytes[i] = (high * 16 + low).toByte()
    }
    return bytes
}