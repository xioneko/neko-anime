package com.xioneko.android.nekoanime.ui.util

class TrackingIterator<T>(private val iterator: Iterator<T>) : Iterator<T> {
    var current: T? = null
        private set

    override fun hasNext(): Boolean {
        return iterator.hasNext()
    }

    override fun next(): T {
        current = iterator.next()
        return current!!
    }
}

fun <T> Iterator<T>.withTracking() = TrackingIterator(this)
