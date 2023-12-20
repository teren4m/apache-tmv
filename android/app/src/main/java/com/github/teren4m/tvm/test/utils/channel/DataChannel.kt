package com.github.teren4m.tvm.test.utils.channel

import android.util.Log
import com.github.teren4m.tvm.test.utils.Optional
import com.github.teren4m.tvm.test.utils.Optional.None
import com.github.teren4m.tvm.test.utils.toOptional
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class DataChannel<T> {
    private val dataFlow = MutableStateFlow<Optional<T>>(None)

    @Suppress("UNCHECKED_CAST")
    val flow: Flow<T> = dataFlow.asStateFlow()
        .filterIsInstance(Optional.Some::class)
        .map { it.x as T }

    suspend fun update(data: T) {
        dataFlow.update {
            val newData = when (it) {
                None -> data
                is Optional.Some -> onDataUpdate(it.x, data)
            }.toOptional()
            newData
        }
    }

    val onDataUpdate: suspend (old: T, new: T) -> T =
        { _, newData -> newData }
}