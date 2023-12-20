package com.github.teren4m.tvm.test.utils

import android.os.SystemClock
import kotlin.math.round

class Fps {

    companion object {
        private const val NO_TIME = -1L
    }

    private var prev: Long = NO_TIME

    fun tick(): Double {
        if (prev == NO_TIME) {
            prev = System.currentTimeMillis()
            return 0.0
        }
        val curr = System.currentTimeMillis()
        val frameTime = 1000.0 / (curr - prev)
        prev = curr
        return round(frameTime)
    }
}