package ru.biozzlab.mylauncher.domain.models

import ru.biozzlab.mylauncher.domain.types.ContainerType

open class ItemCell(
    val container: ContainerType,
    val cellX: Int,
    val cellY: Int,
    val cellHSpan: Int,
    val cellVSpan: Int
) {
    var title: String = ""
}