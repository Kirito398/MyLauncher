package ru.biozzlab.mylauncher.domain.models

import ru.biozzlab.mylauncher.domain.types.ContainerType

open class ItemCell(
    val id: Long,
    val container: ContainerType,
    val packageName: String,
    val className: String,
    var cellX: Int,
    var cellY: Int,
    val desktopNumber: Int = 0,
    val cellHSpan: Int = 1,
    val cellVSpan: Int = 1
)