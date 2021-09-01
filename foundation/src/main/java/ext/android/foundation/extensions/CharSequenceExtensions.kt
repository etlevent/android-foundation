package ext.android.foundation.extensions


fun CharSequence.padLeft(numberOfChars: Number): String {
    return padLeft(numberOfChars, " " as CharSequence)
}

fun CharSequence.padLeft(numberOfChars: Number, padding: CharSequence): String {
    val numChars = numberOfChars.toInt()
    return if (numChars <= length) toString() else getPadding(
            padding.toString(),
            numChars - length
    ) + this
}

private fun getPadding(padding: CharSequence, length: Int): String {
    return if (padding.length < length) padding.multiply(length / padding.length + 1).substring(
            0,
            length
    ) else "" + padding.subSequence(0, length)
}

fun CharSequence.multiply(factor: Number): String {
    val size = factor.toInt()
    if (size == 0) {
        return ""
    } else require(size >= 0) { "multiply() should be called with a number of 0 or greater not: $size" }
    val answer = StringBuilder(this)

    for (i in 1 until size) {
        answer.append(this)
    }

    return answer.toString()
}