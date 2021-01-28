package ru.biozzlab.mylauncher.controllers

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Point
import android.graphics.Rect
import android.view.MotionEvent
import android.view.View
import ru.biozzlab.mylauncher.calculateDistance
import ru.biozzlab.mylauncher.domain.models.DragObject
import ru.biozzlab.mylauncher.domain.models.ItemCell
import ru.biozzlab.mylauncher.domain.types.WorkspaceItemType
import ru.biozzlab.mylauncher.ui.interfaces.DragScroller
import ru.biozzlab.mylauncher.ui.interfaces.DragSource
import ru.biozzlab.mylauncher.ui.interfaces.DropTarget
import ru.biozzlab.mylauncher.ui.layouts.DragLayer
import ru.biozzlab.mylauncher.ui.views.DragView
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class DragController {

    companion object {
        const val SCROLL_ZONE = 40
        const val SCROLL_WAITING_TIME = 500
        const val MAX_LONG_PRESS_DISTANCE = 10
        const val LONG_PRESS_TIME = 700
    }

    private var isDragging = false
    private var motionDownX = 0
    private var motionDownY = 0
    private var motionDownTime = -1L

    private var lastScrollingTime = 0L

    private lateinit var dragObject: DragObject
    private lateinit var dropTarget: DropTarget
    private lateinit var dragScroller: DragScroller

    private var onLongPressListener: (() -> Unit)? = null

    enum class DragAction(id: Int) {
        MOVE(0),
        COPY(1);
    }

    fun setDropTarget(target: DropTarget) {
        dropTarget = target
    }

    fun setDragScroller(scroller: DragScroller) {
        dragScroller = scroller
    }

    fun setOnLongPressListener(listener: () -> Unit) {
        onLongPressListener = listener
    }

    fun startDrag(context: Context, view: View, dragLayer: DragLayer, bitmap: Bitmap, dragLayerX: Int, dragLayerY: Int,
                  dragSource: DragSource, dragAction: DragAction, initialDragViewScale: Float, spanX: Int, spanY: Int) {
        val registrationX = motionDownX - dragLayerX
        val registrationY = motionDownY - dragLayerY

        isDragging = true

        dragObject = DragObject(spanX = spanX, spanY = spanY)
        dragObject.dragSource = dragSource
        dragObject.dragView =
            DragView(context, dragLayer, bitmap, registrationX, registrationY, 0, 0, bitmap.width, bitmap.height, initialDragViewScale)
                .apply { tag = view.tag }

        dragObject.dragView?.show(motionDownX, motionDownY)
    }

    private fun endDrag() {
        if (!isDragging) return
        isDragging = false

        dragObject.dragView?.let { if (!dragObject.deferDragViewCleanupPostAnimation) it.remove() }
        dragObject.dragView = null
    }

    fun onInterceptTouchEvent(event: MotionEvent, dragLayer: DragLayer): Boolean {
        val position = getClampedDragLayerPosition(event.x, event.y, dragLayer)

        when(event.action) {
            MotionEvent.ACTION_DOWN -> {
                motionDownX = position[0]
                motionDownY = position[1]
                motionDownTime = System.currentTimeMillis()
            }
            MotionEvent.ACTION_UP -> endDrag()
        }

        return isDragging
    }

    fun onTouchEvent(event: MotionEvent, dragLayer: DragLayer): Boolean {
        if (!isDragging) return false

        val position = getClampedDragLayerPosition(event.x, event.y, dragLayer)

        when(event.action) {
            MotionEvent.ACTION_DOWN -> {
                motionDownX = position[0]
                motionDownY = position[1]
                motionDownTime = System.currentTimeMillis()
            }
            MotionEvent.ACTION_MOVE -> handleMove(position[0], position[1])
            MotionEvent.ACTION_UP -> {
                if (isDragging) drop(position[0], position[1])
                endDrag()
            }
        }

        return true
    }

    private fun handleMove(x: Int, y: Int) {
        dragObject.dragView?.move(x, y)
        dragObject.x = x
        dragObject.y = y
        dropTarget.onDragOver(dragObject)

        checkLongPress(x, y)
        checkScrollState(x, y)
    }

    private fun checkLongPress(x: Int, y: Int) {
        val distance = calculateDistance(Point(motionDownX, motionDownY), Point(x, y))
        if (distance > MAX_LONG_PRESS_DISTANCE || motionDownTime < 0L) return

        val deltaTime = System.currentTimeMillis() - motionDownTime
        if (deltaTime < LONG_PRESS_TIME) return

        if ((dragObject.dragView?.tag as ItemCell).type == WorkspaceItemType.SHORTCUT)
            onLongPressListener?.invoke()

        motionDownTime = -1
    }

    private fun checkScrollState(x: Int, y: Int) {
        val currentTime = System.currentTimeMillis()
        val delta = abs(lastScrollingTime - currentTime)
        val isScrollWaiting = delta < SCROLL_WAITING_TIME

        if (isScrollWaiting) return

        val dragLayer = dragObject.dragView?.dragLayer ?: return

        when {
            x < SCROLL_ZONE ->
                if (dragScroller.onEnterScrollArea(x, y, 0)) lastScrollingTime = currentTime

            x > dragLayer.width - SCROLL_ZONE ->
                if (dragScroller.onEnterScrollArea(x, y, 1)) lastScrollingTime = currentTime
        }
    }

    private fun drop(x: Int, y: Int) {
        val coordinates = mutableListOf(x, y)

        dragObject.x = coordinates[0]
        dragObject.y = coordinates[1]

        dropTarget.onDragExit(dragObject)

        val accepted = if(dropTarget.acceptDrop(dragObject)) {
            dropTarget.onDrop(dragObject)
            true
        } else false

        dragObject.dragSource.onDropComplete(dropTarget as View, dragObject, false, accepted)
    }

    private fun getClampedDragLayerPosition(x: Float, y: Float, dragLayer: DragLayer): List<Int> {
        val dragLayerRect = Rect()
        dragLayer.getLocalVisibleRect(dragLayerRect)

        val point = mutableListOf<Int>()
        point.add(0, max(dragLayerRect.left, min(x.toInt(), dragLayerRect.right - 1)))
        point.add(1, max(dragLayerRect.top, min(y.toInt(), dragLayerRect.bottom - 1)))
        return point
    }
}