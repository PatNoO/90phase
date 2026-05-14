package com.example.a90phase.util

import android.util.Log

object AppLogger {

    private var debugEnabled = false

    fun init(debug: Boolean) {
        debugEnabled = debug
    }

    fun d(tag: String, message: String) {
        if (debugEnabled) Log.d(tag, message)
    }

    fun i(tag: String, message: String) {
        Log.i(tag, message)
    }

    fun w(tag: String, message: String, throwable: Throwable? = null) {
        if (throwable != null) Log.w(tag, message, throwable) else Log.w(tag, message)
    }

    fun e(tag: String, message: String, throwable: Throwable? = null) {
        if (throwable != null) Log.e(tag, message, throwable) else Log.e(tag, message)
    }
}
