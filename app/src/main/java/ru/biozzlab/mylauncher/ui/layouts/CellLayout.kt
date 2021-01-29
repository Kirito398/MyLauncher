package ru.biozzlab.mylauncher.ui.layouts

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.children
import ru.biozzlab.mylauncher.R
import ru.biozzlab.mylauncher.R.styleable.CellLayout
import ru.biozzlab.mylauncher.calculateDistance
import ru.biozzlab.mylauncher.domain.models.ItemCell
import ru.biozzlab.mylauncher.ui.layouts.params.CellLayoutParams

class CellLayout(context: Context, attributeSet: AttributeSet, defStyle: Int)
    : ViewGroup(context, attributeSet, defStyle) {

    constructor(context: Context, attributeSet: AttributeSet) : this(context, attributeSet, 0)

    companion object {
        const val DEFAULT_COLUMN_COUNT = 4
        const val DEFAULT_ROW_COUNT = 5
        const val DRAG_OUTLINE_PAINT_ALPHA = 125
    }

    private var columnCount: Int = -1
    private var rowCount: Int = -1
    private var cellWidth: Int = -1
    private var cellHeight: Int = -1
    private var widthGap: Int = -1
    private var heightGap: Int = -1

    private var interceptTouchListener: OnTouchListener? = null
    private var dragOutlineBitmap: Bitmap? = null
    private var dragOutlineRect: Rect = Rect(-1, -1, -1, -1)
    private var dragOutlinePaint: Paint

    private val container: CellContainer = CellContainer(context)

    var isHotSeat = false

    init {
        setWillNotDraw(false)

        val attrs = context.obtainStyledAttributes(attributeSet, CellLayout)
        cellWidth = attrs.getDimensionPixelSize(R.styleable.CellLayout_cellWidth, 10)
        cellHeight = attrs.getDimensionPixelSize(R.styleable.CellLayout_cellHeight, 10)
        widthGap = attrs.getDimensionPixelSize(R.styleable.CellLayout_widthGap, 0)
        heightGap = attrs.getDimensionPixelSize(R.styleable.CellLayout_heightGap, 0)
        columnCount = attrs.getInt(R.styleable.CellLayout_columnCount, DEFAULT_COLUMN_COUNT)
        rowCount = attrs.getInt(R.styleable.CellLayout_rowCount, DEFAULT_ROW_COUNT)
        attrs.recycle()

        dragOutlinePaint = Paint()
        dragOutlinePaint.alpha = DRAG_OUTLINE_PAINT_ALPHA

        container.setCellDimensions(cellWidth, cellHeight, widthGap, heightGap)
        container.setColumnCount(columnCount)
        addView(container)
    }

    fun setOnInterceptTouchListener(listener: OnTouchListener) {
        interceptTouchListener = listener
    }

    fun addViewToCell(view: View, index: Int, id: Int, params: CellLayoutParams, markCells: Boolean): Boolean {
        view.scaleX = 1.0F
        view.scaleY = 1.0F

        if (view is TextView) {
            if (params.showText)
                view.setTextColor(ContextCompat.getColor(context, R.color.workspace_icon_text_color))
            else
                view.setTextColor(ContextCompat.getColor(context, R.color.hot_seat_text_color))
        }

        if (params.cellX >= 0 && params.cellX <= columnCount - 1 && params.cellY >= 0 && params.cellY <= rowCount - 1) {
            if (params.cellHSpan < 0) params.cellHSpan = columnCount
            if (params.cellVSpan < 0) params.cellVSpan = rowCount

            view.id = id
            container.addView(view, index, params)

            return true
        }
        return false
    }

    fun setGridSize(x: Int, y: Int) {
        columnCount = x
        rowCount = y
        container.setColumnCount(columnCount)
        requestLayout()
    }

    fun setDragOutlineBitmap(bitmap: Bitmap, position: MutableList<Int>, dragRegion: Rect, isWidget: Boolean) {
        cellToPoint(position[0], position[1], position)

        var left = position[0] //+ (cellWidth - dragRegion.width()) / 2
        val top = position[1]

        if (!isWidget) left += (cellWidth - dragRegion.width()) / 2

        dragOutlineBitmap = bitmap
        dragOutlineRect.set(left, top, left + dragOutlineBitmap!!.width, top + dragOutlineBitmap!!.height)

        invalidate() //TODO проверить как это сделано в исходниках
    }

    fun deleteDragOutlineBitmap() {
        dragOutlineBitmap = null
        dragOutlineRect.set(0, 0, 0, 0)
        invalidate() //TODO проверить как это сделано в исходниках
    }

    private fun cellToPoint(cellX: Int, cellY: Int, position: MutableList<Int>) {
        position[0] = paddingLeft + cellX * (cellWidth + widthGap)
        position[1] = paddingTop + cellY * (cellHeight + heightGap)
    }

    fun findNearestArea(x: Int, y: Int, spanX: Int = 1, spanY: Int = 1): MutableList<Int> {
        var minDistance = Double.MAX_VALUE
        val point = mutableListOf(-1, -1)
        val cellPositions = getCellsPosition()

        for (row in 0 until rowCount) {
            for (column in 0 until columnCount) {
                if (column + spanX > columnCount || row + spanY > rowCount) continue

                var isCellEmpty = true
                for (i in 0 until spanX)
                    for (j in 0 until spanY)
                        if (cellPositions.contains(Pair(column + i, row + j))) isCellEmpty = false

                if (!isCellEmpty) continue

                val cellPosition = mutableListOf(-1, -1)
                cellToPoint(column, row, cellPosition)

                val centerX = x - cellWidth / 2
                val centerY = y - cellHeight / 2

                val distance = calculateDistance(Point(cellPosition[0], cellPosition[1]), Point(centerX, centerY))

                if (distance >= minDistance) continue

                minDistance = distance
                point[0] = column
                point[1] = row
            }
        }

        return point
    }

    private fun getCellsPosition(): MutableList<Pair<Int, Int>> {
        val positions = mutableListOf<Pair<Int, Int>>()

        for (child in container.children) {
            if (child.visibility != View.VISIBLE) continue
            val params = child.layoutParams as CellLayoutParams

            for (i in 0 until params.cellHSpan)
                for (j in 0 until params.cellVSpan)
                    positions.add(Pair(params.cellX + i, params.cellY + j))
        }

        return positions
    }

    override fun onDraw(canvas: Canvas?) {
        dragOutlineBitmap?.let {
            canvas?.drawBitmap(it, null, dragOutlineRect, dragOutlinePaint)
        }
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        for (view in children)
            view.layout(paddingLeft, paddingTop, r - l - paddingRight, b - t - paddingBottom)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var height = MeasureSpec.getSize(heightMeasureSpec)
        var width = MeasureSpec.getSize(widthMeasureSpec)

        if (MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.AT_MOST) {
            width = paddingLeft + paddingRight + columnCount * cellWidth + (columnCount - 1) * widthGap
            height = paddingTop + paddingBottom + rowCount * cellHeight + (rowCount - 1) * heightGap
        }

        for (child in children) {
            val childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(
                width - paddingLeft - paddingRight,
                MeasureSpec.EXACTLY
            )
            val childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(
                height - paddingTop - paddingBottom,
                MeasureSpec.EXACTLY
            )
            child.measure(childWidthMeasureSpec, childHeightMeasureSpec)
        }

        setMeasuredDimension(width, height)
    }

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        return interceptTouchListener?.onTouch(this, ev) ?: false
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        return true
    }

    override fun removeAllViewsInLayout() {
        if (container.childCount <= 0) return
        container.removeAllViewsInLayout()
    }

    override fun generateLayoutParams(attrs: AttributeSet?): ViewGroup.LayoutParams
            = CellLayoutParams(context, attrs)

    override fun generateLayoutParams(p: ViewGroup.LayoutParams?): ViewGroup.LayoutParams
            = CellLayoutParams(p)

    override fun checkLayoutParams(p: ViewGroup.LayoutParams?): Boolean = p is CellLayoutParams

    fun calculateItemDimensions(item: ItemCell, height: Int, width: Int) {
        item.cellHSpan = height / cellHeight
        item.cellVSpan = width / cellWidth
    }
}