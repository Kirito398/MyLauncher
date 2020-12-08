package ru.biozzlab.mylauncher.ui.interfaces

import ru.biozzlab.mylauncher.domain.models.DragObject

interface DropTarget {
    fun onDragExit(dragObject: DragObject)
    fun onDragOver(dragObject: DragObject)
    fun acceptDrop(dragObject: DragObject): Boolean
    fun onDrop(dragObject: DragObject)
}