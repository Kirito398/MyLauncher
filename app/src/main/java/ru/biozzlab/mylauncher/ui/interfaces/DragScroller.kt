package ru.biozzlab.mylauncher.ui.interfaces

interface DragScroller {
    fun onEnterScrollArea(x: Int, y: Int, direction: Int): Boolean
    fun onExitScrollArea(): Boolean
}