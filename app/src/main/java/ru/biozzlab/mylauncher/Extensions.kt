package ru.biozzlab.mylauncher

import android.graphics.Point
import android.util.Log
import kotlin.math.pow
import kotlin.math.sqrt

fun String.easyLog(tag: Any? = null) {
    Log.d(if (tag != null) tag::class.java.simpleName else "TAG", this)
}

fun <T> MutableList<T>.copy(list: MutableList<T>) {
    this.clear()
    this.addAll(list)
}

fun <T> MutableList<T>.copy(): MutableList<T> {
    val copyList = mutableListOf<T>()
    copyList.copy(this)
    return copyList
}

fun calculateDistance(a: Point, b:Point) = sqrt((a.x - b.x).toDouble().pow(2.0) + (a.y - b.y).toDouble().pow(2.0))