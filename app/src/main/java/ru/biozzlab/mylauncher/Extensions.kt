package ru.biozzlab.mylauncher

import android.util.Log

fun String.easyLog(tag: Any?) {
    Log.d(if (tag != null) tag::class.java.simpleName else "TAG", this)
}

fun <T> MutableList<T>.copy(list: MutableList<T>) {
    this.clear()
    this.addAll(list)
}