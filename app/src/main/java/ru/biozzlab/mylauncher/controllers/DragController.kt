package ru.biozzlab.mylauncher.controllers

import android.graphics.Rect
import android.view.MotionEvent
import android.view.MotionEvent.*
import ru.biozzlab.mylauncher.ui.Launcher
import kotlin.math.max
import kotlin.math.min

class DragController(private val launcher: Launcher) {
    private var isDragging = false
    private val dragLayerRect = Rect()

    fun onTouchEvent(event: MotionEvent): Boolean {
        if (!isDragging) return false

        val dragLayerPosition = getClampedDragLayerPosition(event.x, event.y)
        val dragLayerPointX = dragLayerPosition[0]
        val dragLayerPointY = dragLayerPosition[1]

        when (event.action) {
            ACTION_MOVE -> {}
            ACTION_DOWN -> {}
            ACTION_UP -> {}
            ACTION_CANCEL -> cancelDrag()
        }

        return true
    }

    private fun cancelDrag() {
        //TODO("Not yet implemented")
    }

    private fun getClampedDragLayerPosition(x: Float, y: Float): List<Int> {
        //launcher.getDragLayer().getLocalVisibleRect(dragLayerRect)
        return listOf(
            max(dragLayerRect.left, min(x.toInt(), dragLayerRect.right - 1)),
            max(dragLayerRect.top, min(y.toInt(), dragLayerRect.bottom - 1))
        )
    }
}