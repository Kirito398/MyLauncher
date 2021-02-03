package ru.biozzlab.mylauncher.ui.layouts

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import ru.biozzlab.mylauncher.controllers.DragController
import ru.biozzlab.mylauncher.copy
import ru.biozzlab.mylauncher.domain.models.DragObject
import ru.biozzlab.mylauncher.domain.models.ItemCell
import ru.biozzlab.mylauncher.domain.models.ItemShortcut
import ru.biozzlab.mylauncher.domain.models.ItemWidget
import ru.biozzlab.mylauncher.domain.types.ContainerType
import ru.biozzlab.mylauncher.ui.interfaces.DragScroller
import ru.biozzlab.mylauncher.ui.interfaces.DragSource
import ru.biozzlab.mylauncher.ui.interfaces.DropTarget
import ru.biozzlab.mylauncher.ui.layouts.params.CellLayoutParams
import ru.biozzlab.mylauncher.ui.views.PagedView
import kotlin.math.round

class Workspace(context: Context, attributeSet: AttributeSet, defStyle: Int) : PagedView(
    context,
    attributeSet,
    defStyle
), View.OnTouchListener, DragSource, DropTarget, DragScroller {

    constructor(context: Context, attributeSet: AttributeSet) : this(context, attributeSet, 0)

    companion object {
        const val DRAG_BITMAP_PADDING = 2
    }

    private lateinit var dragView: View
    private lateinit var dragOutline: Bitmap
    private lateinit var dragController: DragController
    private lateinit var dragTargetLayout: CellLayout

    private var dropTargetCell = mutableListOf(-1, -1)

    private var onShortcutDataChangedListener: ((ItemCell) -> Unit)? = null
    private var hotSeat: HotSeat? = null

    fun setup(dragController: DragController) {
        this.dragController = dragController
        dragController.setDropTarget(this)
        dragController.setDragScroller(this)
    }

    fun setHotSeat(hotSeat: HotSeat) {
        this.hotSeat = hotSeat
    }

    fun setOnShortcutDataChangedListener(func: (ItemCell) -> Unit) {
        onShortcutDataChangedListener = func
    }

    fun setOnShortcutLongPressListener(listener: (View) -> Unit) {
        dragController.setOnLongPressListener { listener.invoke(dragView) }
    }

    fun startDrag(view: View) {
        if (!view.isInTouchMode) return
        view.visibility = View.INVISIBLE
        view.isClickable = false
        this.dragView = view

        //dragOutline = createDragOutline(view, Canvas(), DRAG_BITMAP_PADDING)
        //dragOutline = (dragView.tag as ItemShortcut).iconBitmap!!

        val item = dragView.tag
        dragOutline = when (item) {
            is ItemShortcut -> item.iconBitmap!!
            is ItemWidget -> createDragOutline(view, Canvas(), DRAG_BITMAP_PADDING)
            else -> createDragOutline(view, Canvas(), DRAG_BITMAP_PADDING)
        }

        beginDragShared(view)
    }

    private fun beginDragShared(view: View) {
        val bitmap = createDragBitmap(view, Canvas(), DRAG_BITMAP_PADDING)
        val width = bitmap.width
        val height = bitmap.height

        val dragLayer = (parent as DragLayer)
        val location = mutableListOf<Float>()
        val scale = dragLayer.getLocationInDragLayer(view, location)
        val dragLayerX = round(location[0] - (width - scale * view.width) / 2).toInt()
        val dragLayerY = round(location[1] - (height - scale * height) / 2 - DRAG_BITMAP_PADDING / 2).toInt()

        val item = dragView.tag as ItemCell
        val spanX = item.cellHSpan
        val spanY = item.cellVSpan

        dragController.startDrag(
            context,
            view,
            dragLayer,
            bitmap,
            dragLayerX,
            dragLayerY,
            this,
            DragController.DragAction.MOVE,
            scale,
            spanX,
            spanY
        )
        bitmap.recycle()
    }

    private fun createDragBitmap(view: View, canvas: Canvas, dragBitmapPadding: Int): Bitmap {
        val bitmap = if (view is TextView) {
            val drawable = view.compoundDrawables[1]
            Bitmap.createBitmap(
                drawable.intrinsicWidth + dragBitmapPadding,
                drawable.intrinsicHeight + dragBitmapPadding,
                Bitmap.Config.ARGB_8888
            )
        } else {
            Bitmap.createBitmap(
                view.width + dragBitmapPadding,
                view.height + dragBitmapPadding,
                Bitmap.Config.ARGB_8888
            )
        }

        canvas.setBitmap(bitmap)
        drawDragView(view, canvas, dragBitmapPadding, true)
        canvas.setBitmap(null)

        return bitmap
    }

    private fun drawDragView(view: View, canvas: Canvas, padding: Int, pruneToDrawable: Boolean) {
        val clipRect = Rect()
        view.getDrawingRect(clipRect)

        canvas.save()
        if (view is TextView && pruneToDrawable) {
            val drawable = view.compoundDrawables[1]
            clipRect.set(
                0,
                0,
                drawable.intrinsicWidth + padding,
                drawable.intrinsicHeight + padding
            )
            canvas.translate(padding / 2F, padding / 2F)
            drawable.draw(canvas)
        } else {
            canvas.translate(-view.scrollX + padding / 2.0F, -view.scrollY + padding / 2.0F)
            canvas.clipRect(clipRect)
            //canvas.clipRect(clipRect, Region.Op.REPLACE)
            view.draw(canvas)
        }
        canvas.restore()
    }

    private fun createDragOutline(view: View, canvas: Canvas, padding: Int): Bitmap {
        //val outlineColor = resources.getColor(android.R.color.white)

        val bitmap = Bitmap.createBitmap(
            view.width + padding,
            view.height + padding,
            Bitmap.Config.ARGB_8888
        )

        canvas.setBitmap(bitmap)
        drawDragView(view, canvas, padding, true)
        canvas.setBitmap(null)

        return bitmap
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        computeScroll()
    }

    override fun onChildViewAdded(parent: View?, child: View?) {
        if (child !is CellLayout)
            throw IllegalArgumentException("A workspace can only have CellLayout children!")

        child.setOnInterceptTouchListener(this)
    }

    override fun onChildViewRemoved(parent: View?, child: View?) {
        //TODO("Not yet implemented")
    }

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        return false
    }

    override fun onDragOver(dragObject: DragObject) {
        dragTargetLayout = getCurrentDragTargetLayout(dragObject)

        dragObject.dragView?.let {
            val targetCell = dragTargetLayout.findNearestArea(dragObject.x, dragObject.y, dragObject.spanX, dragObject.spanY)
            dropTargetCell.copy(targetCell)
            dragTargetLayout.setDragOutlineBitmap(dragOutline, targetCell, it.dragRegion, it.tag is ItemWidget)
        }
    }

    override fun onDragExit(dragObject: DragObject) {
        dragTargetLayout.deleteDragOutlineBitmap()
    }

    override fun acceptDrop(dragObject: DragObject): Boolean = true

    override fun onDrop(dragObject: DragObject) {
        dragObject.deferDragViewCleanupPostAnimation = false
        dragView.visibility = View.VISIBLE
        dragView.isClickable = true
        dragView.isPressed = true

        if (dropTargetCell[0] < 0 || dropTargetCell[1] < 0) return

        val layoutParams = dragView.layoutParams as CellLayoutParams
        layoutParams.cellX = dropTargetCell[0]
        layoutParams.cellY = dropTargetCell[1]
        layoutParams.isDropped = true
        layoutParams.showText = !dragTargetLayout.isHotSeat

        updateView()
        dragView.requestLayout()
    }

    private fun updateView() {
        val item = dragView.tag as ItemCell

        //if (currentPage != shortcutItem.desktopNumber || dragTargetLayout.isHotSeat)
            moveView(dragView)

        item.cellX = dropTargetCell[0]
        item.cellY = dropTargetCell[1]
        item.desktopNumber = currentPage
        item.container = if (dragTargetLayout.isHotSeat) ContainerType.HOT_SEAT else ContainerType.DESKTOP

        if (item is ItemShortcut) {
            (dragView as TextView).setCompoundDrawablesWithIntrinsicBounds(null, item.icon, null, null)
        }

        onShortcutDataChangedListener?.invoke(item)
    }

    private fun moveView(view: View) {
        val fromLayout = view.parent as CellContainer
        val toLayout = dragTargetLayout

        fromLayout.removeView(view)
        toLayout.addViewToCell(view, -1, view.id, view.layoutParams as CellLayoutParams, false)
    }

    override fun onDropComplete(
        target: View,
        dragObject: DragObject,
        isFlingToDelete: Boolean,
        success: Boolean
    ) {
        //TODO("Not yet implemented")
    }

    fun findEmptyArea(position: MutableList<Int>, spanX: Int = 1, spanY: Int = 1): Int {
        var childNumber = -1

        for (i in 0 until childCount) {
            val child: CellLayout = (getChildAt(i) as CellLayout)
            val areaPosition = child.findNearestArea(0, 0, spanX, spanY)

            if (areaPosition[0] >= 0 && areaPosition[1] >= 0) {
                position[0] = areaPosition[0]
                position[1] = areaPosition[1]
                childNumber = i
                break
            }
        }

        return childNumber
    }

    private fun getCurrentDragTargetLayout(dragObject: DragObject): CellLayout {
        var layout = getChildAt(currentPage) as CellLayout

        hotSeat?.let {
            val hitRect = Rect()
            it.getHitRect(hitRect)

            if (hitRect.contains(dragObject.x, dragObject.y))
                layout = it.getCellLayout()
        }

        if (::dragTargetLayout.isInitialized && dragTargetLayout != layout)
            dragTargetLayout.deleteDragOutlineBitmap()

        return layout
    }

    override fun onEnterScrollArea(x: Int, y: Int, direction: Int): Boolean = if (direction == 0) scrollLeft() else scrollRight()

    override fun onExitScrollArea(): Boolean {
        TODO("Not yet implemented")
    }
}