package com.github.teren4m.tvm.test.utils

import com.google.common.collect.EvictingQueue
import java.text.DecimalFormat
import java.util.Queue

class SmaLong(size: Int) {
    private var evictingQueue: Queue<Long> = EvictingQueue.create(size)

    fun add(x: Long): Double {
        evictingQueue.add(x)
        return evictingQueue.sumOf { it.toDouble() } / evictingQueue.size
    }
}

class SmaDouble(size: Int) {
    private var evictingQueue: Queue<Double> = EvictingQueue.create(size)

    fun add(x: Double): Double {
        evictingQueue.add(x)
        val sma = evictingQueue.sumOf { it.toDouble() } / evictingQueue.size
        val df = DecimalFormat("#.#")
        return df.format(sma).toDouble()
    }
}