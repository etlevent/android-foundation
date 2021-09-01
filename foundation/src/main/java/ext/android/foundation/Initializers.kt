package ext.android.foundation

import androidx.startup.Initializer

abstract class NonDependencyInitializer<T> : Initializer<T> {
    final override fun dependencies(): MutableList<Class<out Initializer<*>>> = mutableListOf()
}