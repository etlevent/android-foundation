package ext.android.foundation.extensions

fun <T> List<T>.toArrayList() = ArrayList<T>(size).also { result -> result.addAll(this) }