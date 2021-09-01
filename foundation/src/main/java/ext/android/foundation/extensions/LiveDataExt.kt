package ext.android.foundation.extensions

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import ext.android.foundation.core.BiFunction
import ext.android.foundation.core.Consumer
import ext.android.foundation.core.Predicate
import ext.android.foundation.core.Transformer


object LiveDataTransformers {

    @JvmStatic
    fun <T> doOnNext(source: LiveData<T>, consumer: Consumer<T>): LiveData<T> {
        val result = MediatorLiveData<T>()
        result.addSource(source) {
            consumer(it)
            result.value = it
        }
        return result
    }

    @JvmStatic
    fun <T> filter(source: LiveData<T>, predicate: Predicate<T>): LiveData<T> {
        val result = MediatorLiveData<T>()
        result.addSource(source) {
            if (predicate(it)) {
                result.value = it
            }
        }
        return result
    }

    fun <X, Y, Z> zip(
        first: LiveData<X>,
        second: LiveData<Y>,
        zipper: BiFunction<X, Y, Z>
    ): LiveData<Z> {
        val result = MediatorLiveData<Z>()
        var firstEmitted = false
        var firstValue: X? = null

        var secondEmitted = false
        var secondValue: Y? = null

        fun sendZippedValue() {
            if (firstEmitted && secondEmitted) {
                result.value = zipper(firstValue!!, secondValue!!)
                firstEmitted = false
                secondEmitted = false
            }
        }

        result.addSource(first) { value ->
            firstEmitted = true
            firstValue = value
            sendZippedValue()
        }
        result.addSource(second) { value ->
            secondEmitted = true
            secondValue = value
            sendZippedValue()
        }
        return result
    }

    fun <X, Y, Z> combineLatest(
        first: LiveData<X>,
        second: LiveData<Y>,
        combiner: BiFunction<X, Y, Z>
    ): LiveData<Z> {
        val result = MediatorLiveData<Z>()
        var firstValue: X? = null
        var secondValue: Y? = null

        fun sendCombinedValue() {
            if (firstValue != null && secondValue != null) {
                result.value = combiner(firstValue!!, secondValue!!)
            }
        }
        result.addSource(first) { value ->
            firstValue = value
            sendCombinedValue()
        }
        result.addSource(second) { value ->
            secondValue = value
            sendCombinedValue()
        }
        return result
    }
}

fun <T> LiveData<T>.doOnNext(consumer: Consumer<T>): LiveData<T> =
    LiveDataTransformers.doOnNext(this, consumer)

inline fun <T> LiveData<T>.filter(crossinline predicate: Predicate<T>): LiveData<T> =
    LiveDataTransformers.filter(this) { predicate(it) }

inline fun <T> LiveData<T>.filterNot(crossinline predicate: Predicate<T>): LiveData<T> =
    LiveDataTransformers.filter(this) { !predicate(it) }

inline fun <X, Y, Z> LiveData<X>.zip(
    other: LiveData<Y>,
    crossinline zipper: BiFunction<X, Y, Z>
): LiveData<Z> = LiveDataTransformers.zip(this, other) { x, y -> zipper(x, y) }

inline fun <X, Y, Z> LiveData<X>.combineLatest(
    other: LiveData<Y>,
    crossinline combiner: BiFunction<X, Y, Z>
): LiveData<Z> = LiveDataTransformers.combineLatest(this, other) { x, y -> combiner(x, y) }

fun <T : Any> T.liveData(): LiveData<T> = MutableLiveData(this)

// FIXME: 20-10-19 Fix Universal Combine Latest LiveData. like Observable combineLatest
class CombineLatestLiveData<T, R>(
    private val sources: List<LiveData<out T>>,
    private val combiner: Transformer<List<*>, out R>
) : MediatorLiveData<R>()