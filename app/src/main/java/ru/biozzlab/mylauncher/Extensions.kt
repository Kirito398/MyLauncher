package ru.biozzlab.mylauncher

import android.util.Log

fun String.easyLog(tag: String?) {
    Log.d(tag ?: "TAG", this)
}

fun <T> MutableList<T>.copy(list: MutableList<T>) {
    this.clear()
    this.addAll(list)
}