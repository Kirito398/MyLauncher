package ru.biozzlab.mylauncher.domain.models

import ru.biozzlab.mylauncher.domain.types.ContainerType
import ru.biozzlab.mylauncher.domain.types.WorkspaceItemType

open class ItemCell(
    val id: Long,
    val type: WorkspaceItemType,
    var container: ContainerType,
    val packageName: String,
    val className: String,
    var cellX: Int,
    var cellY: Int,
    var desktopNumber: Int = 0,
    val cellHSpan: Int = 1,
    val cellVSpan: Int = 1,
    val appWidgetId: Int = -1
)