package ru.biozzlab.mylauncher.ui.layouts

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import androidx.core.view.children
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

    private var onItemCellDataChangedListener: ((ItemCell) -> Unit)? = null
    private var onItemDeleteListener: ((ItemCell) -> Unit)? = null

    private lateinit var hotSeat: HotSeat
    private lateinit var deleteRegion: View

    fun setup(dragController: DragController , hotSeat: HotSeat, deleteView: View) {
        this.dragController = dragController
        dragController.setDropTarget(this)
        dragController.setDragScroller(this)

        this.hotSeat = hotSeat
        this.deleteRegion = deleteView
    }

    fun setOnItemCellDataChangedListener(func: (ItemCell) -> Unit) {
        onItemCellDataChangedListener = func
    }

    fun setOnShortcutLongPressListener(listener: (View) -> Unit) {
        dragController.setOnLongPressListener { listener.invoke(dragView) }
    }

    fun setOnItemDeleteListener(listener: (ItemCell) -> Unit) {
        onItemDeleteListener = listener
    }

    fun startDrag(view: View) {
        if (!view.isInTouchMode) return
        view.visibility = View.INVISIBLE
        view.isClickable = false
        this.dragView = view

        showDeleteRegion(true)

        beginDragShared(view)
    }

    private fun beginDragShared(view: View) {
        dragOutline = createDragBitmap(view, Canvas(), DRAG_BITMAP_PADDING)
        val width = dragOutline.width
        val height = dragOutline.height

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
            dragOutline,
            dragLayerX,
            dragLayerY,
            this,
            DragController.DragAction.MOVE,
            scale,
            spanX,
            spanY
        )
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
            val isWidget = it.tag is ItemWidget
            val targetCell = dragTargetLayout.findNearestArea(dragObject.x, dragObject.y, dragObject.spanX, dragObject.spanY)
            dropTargetCell.copy(targetCell)
            dragTargetLayout.setDragOutlineBitmap(dragOutline, targetCell, it.dragRegion, isWidget)
        }
    }

    override fun onDragExit(dragObject: DragObject) {
        dragTargetLayout.deleteDragOutlineBitmap()
        showDeleteRegion(false)
    }

    override fun acceptDrop(dragObject: DragObject): Boolean = true

    override fun onDrop(dragObject: DragObject) {
        dragObject.deferDragViewCleanupPostAnimation = false
        dragView.visibility = View.VISIBLE
        dragView.isClickable = true
        dragView.isPressed = true

        if (dropTargetCell[0] < 0 || dropTargetCell[1] < 0) return

        if (checkInDeleteRegion(dragObject)) {
            val item = dragView.tag as ItemCell
            onItemDeleteListener?.invoke(item)
            if (item is ItemWidget) (dragView.parent as CellContainer).removeView(dragView)
            return
        }

        val layoutParams = dragView.layoutParams as CellLayoutParams
        layoutParams.cellX = dropTargetCell[0]
        layoutParams.cellY = dropTargetCell[1]
        layoutParams.isDropped = true
        layoutParams.showText = !dragTargetLayout.isHotSeat

        moveView(dragView)
        updateView()
        dragView.requestLayout()
    }

    private fun checkInDeleteRegion(dragObject: DragObject): Boolean {
        val hitRect = Rect()
        deleteRegion.getHitRect(hitRect)
        return hitRect.contains(dragObject.x, dragObject.y)
    }

    private fun updateView() {
        val item = dragView.tag as ItemCell

        item.cellX = dropTargetCell[0]
        item.cellY = dropTargetCell[1]
        item.desktopNumber = currentPage
        item.container = if (dragTargetLayout.isHotSeat) ContainerType.HOT_SEAT else ContainerType.DESKTOP

        if (item is ItemShortcut)
            (dragView as TextView).setCompoundDrawablesWithIntrinsicBounds(null, item.icon, null, null)

        onItemCellDataChangedListener?.invoke(item)
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

        reserveCell(childNumber, position, spanX, spanY)

        return childNumber
    }

    private fun reserveCell(childNumber: Int, position: List<Int>, spanX: Int = 1, spanY: Int = 1) {
        if (childNumber < 0 || position[0] < 0 || position[1] < 0) return
        val layout = getChildAt(childNumber) as CellLayout
        layout.setCellsReserved(position[0], position[1], spanX, spanY)
    }

    fun removeViewWithPackages(packageName: String): ItemCell? {
        for (child in children) {
            val view = (child as CellLayout).findViewInContainer(packageName) ?: continue
            child.removeViewFromContainer(view)
            return view.tag as ItemCell
        }
        return null
    }

    private fun getCurrentDragTargetLayout(dragObject: DragObject): CellLayout {
        var layout = getChildAt(currentPage) as CellLayout

        hotSeat.let {
            val hitRect = Rect()
            it.getHitRect(hitRect)

            if (hitRect.contains(dragObject.x, dragObject.y))
                layout = it.getCellLayout()
        }

        if (::dragTargetLayout.isInitialized && dragTargetLayout != layout)
            dragTargetLayout.deleteDragOutlineBitmap()

        return layout
    }

    private fun showDeleteRegion(show: Boolean) {
        deleteRegion.visibility = if (show) View.VISIBLE else View.INVISIBLE
    }

    override fun onEnterScrollArea(x: Int, y: Int, direction: Int): Boolean = if (direction == 0) scrollLeft() else scrollRight()

    override fun onExitScrollArea(): Boolean {
        TODO("Not yet implemented")
    }
}