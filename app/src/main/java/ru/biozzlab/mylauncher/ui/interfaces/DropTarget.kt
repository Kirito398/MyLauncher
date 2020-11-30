package ru.biozzlab.mylauncher.ui.interfaces

import android.graphics.Rect
import ru.biozzlab.mylauncher.domain.models.DragObject

interface DropTarget {
    fun onDragExit(dragObject: DragObject)
    fun onDragOver(dragObject: DragObject)
    fun acceptDrop(dragObject: DragObject): Boolean
    fun onDrop(dragObject: DragObject)
    fun getHitRect(rect: Rect)
    fun getLocationInDragLayer(location: MutableList<Float>)
    fun getLeft(): Int
    fun getTop(): Int
}