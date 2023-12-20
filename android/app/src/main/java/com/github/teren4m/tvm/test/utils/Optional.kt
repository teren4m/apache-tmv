package com.github.teren4m.tvm.test.utils

sealed class Optional<out T> {
    data object None : Optional<Nothing>()
    data class Some<T>(val x: T) : Optional<T>()
}

fun <T> T.toOptional(): Optional<T> {
    return Optional.Some(this)
}