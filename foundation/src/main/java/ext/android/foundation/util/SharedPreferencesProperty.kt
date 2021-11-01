package ext.android.foundation.util

import android.content.SharedPreferences
import ext.android.foundation.extensions.getSharedPreferencesValue
import ext.android.foundation.extensions.setSharedPreferencesValue
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

abstract class SharedPreferencesDelegate {
    abstract fun getSharedPreferences(): SharedPreferences

    fun <T : Any> property(
        key: String? = null,
        defaultValue: T? = null
    ) = SharedPreferenceProperty(key, defaultValue)

    class SharedPreferenceProperty<T : Any>(
        private val key: String? = null,
        private val defaultValue: T? = null
    ) : ReadWriteProperty<SharedPreferencesDelegate, T> {
        private var _value: T? = null
        override fun getValue(thisRef: SharedPreferencesDelegate, property: KProperty<*>): T {
            val propName: String = if (key.isNullOrEmpty()) property.name else key
            if (_value == null)
                _value = getSharedPreferencesValue(
                    property.returnType.classifier,
                    thisRef.getSharedPreferences(),
                    propName,
                    defaultValue
                )
            return _value!!
        }

        override operator fun setValue(
            thisRef: SharedPreferencesDelegate,
            property: KProperty<*>,
            value: T
        ) {
            if (_value != value) {
                _value = value
                val propName: String = if (key.isNullOrEmpty()) property.name else key
                setSharedPreferencesValue(
                    property.returnType.classifier,
                    thisRef.getSharedPreferences(),
                    propName,
                    value
                )
            }
        }
    }
}