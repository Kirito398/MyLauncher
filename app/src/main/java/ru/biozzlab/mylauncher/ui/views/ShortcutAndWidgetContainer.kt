package ru.biozzlab.mylauncher.ui.views

import android.app.WallpaperManager
import android.app.WallpaperManager.COMMAND_DROP
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import androidx.core.view.children

class ShortcutAndWidgetContainer(context: Context, attributeSet: AttributeSet?, defStyle: Int) : ViewGroup(context, attributeSet, defStyle) {
    constructor(context: Context, attributeSet: AttributeSet) : this(context, attributeSet, 0)
    constructor(context: Context) : this(context, null, 0)

    private var cellWidth: Int = -1
    private var cellHeight: Int = -1
    private var widthGap: Int = -1
    private var heightGap: Int = -1
    private var columnCount: Int = -1

    fun setCellDimensions(cellWidth: Int, cellHeight: Int, widthGap: Int, heightGap: Int, columnCount: Int) {
        this.cellWidth = cellWidth
        this.cellHeight = cellHeight
        this.widthGap = widthGap
        this.heightGap = heightGap
        this.columnCount = columnCount
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        for (child in children) {
            if (child.visibility == GONE) continue

            val layoutParams = child.layoutParams as CellLayout.LayoutParams
            child.layout(layoutParams.x, layoutParams.y, layoutParams.x + layoutParams.width, layoutParams.y + layoutParams.height)

            if (layoutParams.isDropped) {
                layoutParams.isDropped = false

                val cellPosition = intArrayOf(0, 0)
                getLocationOnScreen(cellPosition)
                WallpaperManager.getInstance(context).sendWallpaperCommand(
                    windowToken,
                    COMMAND_DROP,
                    cellPosition[0] + layoutParams.x / 2,
                    cellPosition[1] + layoutParams.y / 2,
                    0,
                    null)
            }
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        for (child in children)
            measureChild(child)

        setMeasuredDimension(
            MeasureSpec.getSize(widthMeasureSpec),
            MeasureSpec.getSize(heightMeasureSpec)
        )
    }

    private fun measureChild(child: View) {
        val layoutParams = child.layoutParams as CellLayout.LayoutParams

        layoutParams.setup(cellWidth, cellHeight, widthGap, heightGap, invertLayoutHorizontally(), columnCount)

        val heightMeasureSpec = MeasureSpec.makeMeasureSpec(layoutParams.width, MeasureSpec.EXACTLY)
        val widthMeasureSpec = MeasureSpec.makeMeasureSpec(layoutParams.height, MeasureSpec.EXACTLY)
        child.measure(widthMeasureSpec, heightMeasureSpec)
    }

    private fun invertLayoutHorizontally(): Boolean {
        return true
    }
}