package ru.biozzlab.mylauncher.domain.models

import ru.biozzlab.mylauncher.domain.types.ContainerType
import ru.biozzlab.mylauncher.domain.types.WorkspaceItemType

open class ItemCell(
    val id: Long = -1,
    val type: WorkspaceItemType,
    var container: ContainerType,
    val packageName: String,
    val className: String,
    var cellX: Int = -1,
    var cellY: Int = -1,
    var desktopNumber: Int = -1,
    var cellHSpan: Int = 1,
    var cellVSpan: Int = 1
)