package com.github.teren4m.tvm.test.utils

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

inline fun <T, R> Flow<T>.mapFirst(crossinline transform: suspend (value: T) -> R): Flow<R> =
    channelFlow<R> {
        val outerScope = this
        val busy = AtomicBoolean(false)

        collect { x ->
            if (busy.compareAndSet(false, true)) {
                launch(start = CoroutineStart.UNDISPATCHED) {
                    try {
                        send(transform(x))
                        busy.set(false)
                    } catch (e: CancellationException) {
                        // cancel outer scope on cancellation exception, too
                        outerScope.cancel(e)
                    }
                }
            }
        }
    }