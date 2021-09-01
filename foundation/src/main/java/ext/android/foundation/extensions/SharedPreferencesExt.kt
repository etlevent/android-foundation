package ext.android.foundation.extensions

import android.content.SharedPreferences
import androidx.core.content.edit
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KClassifier
import kotlin.reflect.KProperty

fun <T : Any> SharedPreferences.property(
    key: String? = null,
    defaultValue: T? = null
) = SharedPreferencesProperty(this, key, defaultValue)

fun <T : Any> getSharedPreferencesValue(
    classifier: KClassifier?,
    sharedPreferences: SharedPreferences,
    key: String,
    defaultValue: T? = null
): T = when (classifier) {
    String::class -> sharedPreferences.getString(key, (defaultValue ?: "") as String)
    Boolean::class -> sharedPreferences.getBoolean(
        key,
        (defaultValue ?: false) as Boolean
    )
    Long::class -> sharedPreferences.getLong(key, (defaultValue ?: 0) as Long)
    Int::class -> sharedPreferences.getInt(key, (defaultValue ?: 0) as Int)
    Float::class -> sharedPreferences.getFloat(key, (defaultValue ?: .0F) as Float)
    else -> throw UnsupportedOperationException("Unsupported Type [${classifier}]")
} as T

fun <T : Any> setSharedPreferencesValue(
    classifier: KClassifier?,
    sharedPreferences: SharedPreferences,
    key: String,
    value: T
) {
    sharedPreferences.edit {
        when (classifier) {
            String::class -> putString(key, value as String)
            Boolean::class -> putBoolean(key, value as Boolean)
            Long::class -> putLong(key, value as Long)
            Int::class -> putInt(key, value as Int)
            Float::class -> putFloat(key, value as Float)
            else -> throw UnsupportedOperationException("Unsupported Type [${classifier}]")
        }
    }
}

class SharedPreferencesProperty<T : Any>(
    private val sharedPreferences: SharedPreferences,
    private val key: String? = null,
    private val defaultValue: T? = null
) : ReadWriteProperty<Any, T> {
    private var _value: T? = null
    override fun getValue(thisRef: Any, property: KProperty<*>): T {
        val propName: String = if (key.isNullOrEmpty()) property.name else key
        if (_value == null)
            _value = getSharedPreferencesValue(
                property.returnType.classifier,
                sharedPreferences,
                propName,
                defaultValue
            )
        return _value!!
    }

    override operator fun setValue(
        thisRef: Any,
        property: KProperty<*>,
        value: T
    ) {
        if (_value != value) {
            _value = value
            val propName: String = if (key.isNullOrEmpty()) property.name else key
            setSharedPreferencesValue(
                property.returnType.classifier,
                sharedPreferences,
                propName,
                value
            )
        }
    }
}