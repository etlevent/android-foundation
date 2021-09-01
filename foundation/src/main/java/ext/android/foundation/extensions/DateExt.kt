@file:JvmName("DateUtils")

package ext.android.foundation.extensions


import java.text.SimpleDateFormat
import java.util.*
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

const val FORMAT_YMD = "yyyy-MM-dd"
const val FORMAT_YMD_HM = "yyyy-MM-dd HH:mm"
const val FORMAT_YMD_HMS = "yyyy-MM-dd HH:mm:ss"
const val FORMAT_HMS = "HH:mm:ss"
const val FORMAT_HM = "HH:mm"

@JvmOverloads
fun Date.format(
    format: String = FORMAT_YMD_HMS,
    timeZone: TimeZone = TimeZone.getDefault()
): String {
    val formatter = SimpleDateFormat(format, Locale.getDefault())
    formatter.timeZone = timeZone
    return formatter.format(this)
}

fun Date.daysBetween(other: Date): List<Date> {
    val result = ArrayList<Date>()
    val calStart = Calendar.getInstance(Locale.getDefault())
    val calEnd = Calendar.getInstance(Locale.getDefault())
    if (before(other)) {
        calStart.time = this
        calEnd.time = other
    } else {
        calStart.time = other
        calEnd.time = this
    }
    calEnd.add(Calendar.DATE, +1)
    while (calStart.before(calEnd)) {
        result.add(calStart.time)
        calStart.add(Calendar.DAY_OF_YEAR, 1)
    }
    return result
}

@ExperimentalTime
operator fun Date.plus(duration: Duration): Date {
    val calendar = Calendar.getInstance()
    val milliseconds: Long = time + duration.inWholeMilliseconds
    calendar.timeInMillis = milliseconds
    return calendar.time
}

@ExperimentalTime
operator fun Date.minus(duration: Duration): Date {
    val calendar = Calendar.getInstance()
    val milliseconds: Long = time - duration.inWholeMilliseconds
    calendar.timeInMillis = milliseconds
    return calendar.time
}