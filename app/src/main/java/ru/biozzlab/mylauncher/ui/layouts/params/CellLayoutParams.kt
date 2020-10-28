package ru.biozzlab.mylauncher.ui.layouts.params

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup

class CellLayoutParams : ViewGroup.MarginLayoutParams {
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
    constructor(source: CellLayoutParams) : super(source)
    constructor(cellX: Int, cellY: Int, cellHSpan: Int, cellVSpan: Int) : super(MATCH_PARENT, MATCH_PARENT) {
        this.cellX = cellX
        this.cellY = cellY
        this.cellHSpan = cellHSpan
        this.cellVSpan = cellVSpan
    }

    fun setup(cellWidth: Int, cellHeight: Int, widthGap: Int, heightGap: Int, invertHorizontally: Boolean, colCount: Int) {
        if (!isLookedToGrid) return

        var mX = cellX
        if (invertHorizontally)
            mX = colCount - mX - cellHSpan

        width = cellHSpan * cellWidth + (cellHSpan - 1) * widthGap - leftMargin - rightMargin
        height = cellVSpan * cellHeight + (cellVSpan - 1) * heightGap - topMargin - bottomMargin
        x = mX * (cellWidth + widthGap) + leftMargin
        y = cellY * (cellHeight + heightGap) + topMargin
    }
}