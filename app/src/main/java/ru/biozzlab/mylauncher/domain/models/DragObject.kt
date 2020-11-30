package ru.biozzlab.mylauncher.domain.models

import ru.biozzlab.mylauncher.ui.interfaces.DragSource
import ru.biozzlab.mylauncher.ui.views.DragView

data class DragObject(
    var x: Int = -1,
    var y: Int = -1
) {
    lateinit var dragSource: DragSource
    var dragView: DragView? = null
    var deferDragViewCleanupPostAnimation = true
}