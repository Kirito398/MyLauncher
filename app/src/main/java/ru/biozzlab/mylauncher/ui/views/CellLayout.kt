package ru.biozzlab.mylauncher.ui.views

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import androidx.core.view.children
import ru.biozzlab.mylauncher.R
import ru.biozzlab.mylauncher.R.styleable.CellLayout

class CellLayout(context: Context, attributeSet: AttributeSet, defStyle: Int)
    : ViewGroup(context, attributeSet, defStyle) {

    constructor(context: Context, attributeSet: AttributeSet) : this(context, attributeSet, 0)

    private var cellWidth: Int = 0
    private var cellHeight: Int = 0
    private var widthGap: Int = 0
    private var heightGap: Int = 0
    private var columnCount: Int = -1
    private var rowCount: Int = -1

    private var isHotSeat = false
    private var isDragOverlapping = false
    private val container: ShortcutAndWidgetContainer = ShortcutAndWidgetContainer(context)

    init {
        val attrs = context.obtainStyledAttributes(attributeSet, CellLayout)
        cellWidth = attrs.getDimensionPixelSize(R.styleable.CellLayout_cellWidth, 10)
        cellHeight = attrs.getDimensionPixelSize(R.styleable.CellLayout_cellHeight, 10)
        widthGap = attrs.getDimensionPixelSize(R.styleable.CellLayout_widthGap, 0)
        heightGap = attrs.getDimensionPixelSize(R.styleable.CellLayout_heightGap, 0)
        attrs.recycle()

        container.setCellDimensions(cellWidth, cellHeight, widthGap, heightGap, columnCount)
        addView(container)
    }

    fun addViewToCell(view: View, index: Int, id: Int, params: LayoutParams, markCells: Boolean): Boolean {
        view.scaleX = 1.0F
        view.scaleY = 1.0F

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
        container.setCellDimensions(cellWidth, cellHeight, widthGap, heightGap, columnCount)
        requestLayout()
    }

    fun setIsHotSeat(isHotSeat: Boolean) {
        this.isHotSeat = isHotSeat
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        for (view in children)
            view.layout(paddingLeft, paddingTop, r - l - paddingRight, b - t - paddingBottom)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthSpecSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightSpecSize = MeasureSpec.getSize(heightMeasureSpec)

        for (child in children) {
            val childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(
                widthSpecSize - paddingLeft - paddingRight,
                MeasureSpec.EXACTLY
            )
            val childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(
                heightSpecSize - paddingTop - paddingBottom,
                MeasureSpec.EXACTLY
            )
            child.measure(childWidthMeasureSpec, childHeightMeasureSpec)
        }

        setMeasuredDimension(widthSpecSize, heightSpecSize)
    }

    fun getIsDragOverlapping() = isDragOverlapping

    override fun removeAllViewsInLayout() {
        if (container.childCount <= 0) return
        container.removeAllViewsInLayout()
    }

    override fun generateLayoutParams(attrs: AttributeSet?): ViewGroup.LayoutParams
            = LayoutParams(context, attrs)

    override fun generateLayoutParams(p: ViewGroup.LayoutParams?): ViewGroup.LayoutParams
            = LayoutParams(p)

    override fun checkLayoutParams(p: ViewGroup.LayoutParams?): Boolean = p is LayoutParams

    class LayoutParams : MarginLayoutParams {
        var x: Int = 0
        var y: Int = 0
        var cellX = 0
        var cellY = 0
        var cellHSpan = -1
        var cellVSpan = -1
        var isDropped = false
        var isLookedToGrid = true

        constructor(context: Context, attributeSet: AttributeSet?) : super(context, attributeSet)
        constructor(source: ViewGroup.LayoutParams?) : super(source)
        constructor(source: LayoutParams) : super(source)
        constructor(cellX: Int, cellY: Int, cellHSpan: Int, cellVSpan: Int) : super(MATCH_PARENT, MATCH_PARENT) {
            this.cellX = cellX
            this.cellY = cellY
            this.cellHSpan = cellHSpan
            this.cellVSpan = cellVSpan
        }

        fun setup(cellWidth: Int, cellHeight: Int, widthGap: Int, heightGap: Int, invertHorizontally: Boolean, colCount: Int) {
            if (!isLookedToGrid) return

            var hSpan = cellHSpan
            var vSpan = cellVSpan
            var mX = cellX
            var mY = cellY

            if (invertHorizontally)
                mX = colCount - mX - cellHSpan

            width = hSpan * cellWidth + (hSpan - 1) * widthGap - leftMargin - rightMargin
            height = vSpan * cellHeight + (vSpan - 1) * heightGap - topMargin - bottomMargin
            x = mX * (cellWidth + widthGap) + leftMargin
            y = mY * (cellHeight + heightGap) + topMargin
        }
    }
}