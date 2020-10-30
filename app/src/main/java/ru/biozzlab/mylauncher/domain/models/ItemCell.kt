package ru.biozzlab.mylauncher.domain.models

import ru.biozzlab.mylauncher.domain.types.ContainerType

open class ItemCell(
    val container: ContainerType,
    val packageName: String,
    val className: String,
    val cellX: Int,
    val cellY: Int,
    val cellHSpan: Int = 1,
    val cellVSpan: Int = 1
) {
    var title: String = ""
}