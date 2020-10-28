package ru.biozzlab.mylauncher.ui.layouts

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.solver.widgets.analyzer.BasicMeasure
import androidx.core.view.children
import ru.biozzlab.mylauncher.R
import ru.biozzlab.mylauncher.R.styleable.CellLayout
import ru.biozzlab.mylauncher.ui.layouts.params.CellLayoutParams

class CellLayout(context: Context, attributeSet: AttributeSet, defStyle: Int)
    : ViewGroup(context, attributeSet, defStyle) {

    constructor(context: Context, attributeSet: AttributeSet) : this(context, attributeSet, 0)

    private var columnCount: Int = -1
    private var rowCount: Int = -1
    private var cellWidth: Int = -1
    private var cellHeight: Int = -1
    private var widthGap: Int = -1
    private var heightGap: Int = -1
    private var isDragOverlapping = false

    private val container: CellContainer = CellContainer(context)

    init {
        val attrs = context.obtainStyledAttributes(attributeSet, CellLayout)
        cellWidth = attrs.getDimensionPixelSize(R.styleable.CellLayout_cellWidth, 10)
        cellHeight = attrs.getDimensionPixelSize(R.styleable.CellLayout_cellHeight, 10)
        widthGap = attrs.getDimensionPixelSize(R.styleable.CellLayout_widthGap, 0)
        heightGap = attrs.getDimensionPixelSize(R.styleable.CellLayout_heightGap, 0)
        attrs.recycle()

        container.setCellDimensions(cellWidth, cellHeight, widthGap, heightGap)
        container.setColumnCount(columnCount)
        addView(container)
    }

    fun addViewToCell(view: View, index: Int, id: Int, params: CellLayoutParams, markCells: Boolean): Boolean {
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
        container.setColumnCount(columnCount)
        requestLayout()
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        for (view in children)
            view.layout(paddingLeft, paddingTop, r - l - paddingRight, b - t - paddingBottom)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthSpecSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightSpecSize = MeasureSpec.getSize(heightMeasureSpec)
        val widthSpecMode = MeasureSpec.getMode(widthMeasureSpec)

        var height = heightSpecSize
        var width = widthSpecSize
        if (widthSpecMode == MeasureSpec.AT_MOST) {
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

    fun getIsDragOverlapping() = isDragOverlapping

    override fun removeAllViewsInLayout() {
        if (container.childCount <= 0) return
        container.removeAllViewsInLayout()
    }

    override fun generateLayoutParams(attrs: AttributeSet?): ViewGroup.LayoutParams
            = CellLayoutParams(context, attrs)

    override fun generateLayoutParams(p: ViewGroup.LayoutParams?): ViewGroup.LayoutParams
            = CellLayoutParams(p)

    override fun checkLayoutParams(p: ViewGroup.LayoutParams?): Boolean = p is CellLayoutParams
}