package ext.android.foundation.extensions

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class AutoClearedValue<T : Any>(lifecycleOwner: LifecycleOwner, defaultValue: T? = null) :
    ReadWriteProperty<LifecycleOwner, T> {
    private var _value: T? = defaultValue

    init {
        lifecycleOwner.lifecycle.addObserver(object : LifecycleObserver {
            @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
            fun onDestroy(owner: LifecycleOwner) {
                _value = null
            }
        })
    }

    override fun getValue(thisRef: LifecycleOwner, property: KProperty<*>): T {
        return _value
            ?: throw IllegalStateException("should never call auto-cleared-value get when it might not be available")
    }

    override fun setValue(thisRef: LifecycleOwner, property: KProperty<*>, value: T) {
        _value = value
    }
}

fun <T : Any> LifecycleOwner.autoCleared(defaultValue: T? = null) =
    AutoClearedValue(this, defaultValue)