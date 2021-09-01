package ext.android.foundation.core

typealias Consumer<T> = (T) -> Unit
typealias BiConsumer<T, U> = (T, U) -> Unit
typealias Predicate<T> = (T) -> Boolean
typealias Transformer<T, R> = (T) -> R
typealias BiFunction<T, U, R> = (T, U) -> R