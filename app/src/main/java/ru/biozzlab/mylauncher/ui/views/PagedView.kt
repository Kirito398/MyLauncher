package ru.biozzlab.mylauncher.ui.views

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import java.lang.IllegalStateException

abstract class PagedView(context: Context, attrs: AttributeSet, defStyle: Int)
    : ViewGroup(context, attrs, defStyle) {

    constructor(context: Context, attrs: AttributeSet) : this(context, attrs, 0)

    private val childRelativeOffset = mutableListOf<Int>()
    private var layoutScale = 1.0F
    private var nextPage = -1
    private var currentPage = -1
    private var pageSpacingValue: Int = 0
    private var isFirstLayout = true

    fun getNextPage() = if (nextPage != -1) nextPage else currentPage

    init {
        val count = childCount
        for (i in 0 .. count)
            childRelativeOffset.add(i, -1)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        val verticalPadding = paddingTop + paddingBottom
        val childCount = childCount
        val isRtl = isLayoutRtl()

        val startIndex = if (isRtl) childCount - 1 else 0
        val endIndex = if (isRtl) -1 else childCount
        val delta = if (isRtl) -1 else 1

        var childLeft = getRelativeChildOffset(startIndex)
        for (i in startIndex until endIndex step delta) {
            val child = getChildAt(i)
            if (child.visibility == GONE) continue
            
            val childWidth = getScaledMeasuredWidth(child)
            val childHeight = child.measuredHeight
            val childTop = paddingTop + ((measuredHeight - verticalPadding) - childHeight) / 2

            child.layout(childLeft, childTop, child.measuredWidth, childTop + childHeight)
            childLeft += childWidth + pageSpacingValue
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        //val width = MeasureSpec.getSize(widthMeasureSpec)

        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        if (widthMode != MeasureSpec.EXACTLY)
            throw IllegalStateException("Workspace can only be used in EXACTLY mode")

        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        if (heightMode != MeasureSpec.EXACTLY)
            throw IllegalStateException("Workspace can only be used in EXACTLY mode")

        for (i in 0 until childCount)
            getChildAt(i).measure(widthMeasureSpec, heightMeasureSpec)

        if (isFirstLayout) {
            isHorizontalScrollBarEnabled = true
            isFirstLayout = false
        }
    }

    private fun getScaledMeasuredWidth(child: View): Int {
        val measuredWidth = child.measuredWidth
        val maxWidth = if (measuredWidth < 0) 0 else measuredWidth
        return (maxWidth * layoutScale + 0.5F).toInt()
    }

    private fun getRelativeChildOffset(index: Int): Int {
        if (childRelativeOffset.isNotEmpty() && childRelativeOffset[index] != -1)
            return childRelativeOffset[index]

        val padding = paddingLeft + paddingRight
        val offset = paddingLeft + (measuredWidth - padding - getChildWidth(index)) / 2
        childRelativeOffset[index] = offset

        return offset
    }

    private fun getChildWidth(index: Int): Int {
        val measureWidth = getChildAt(index).measuredWidth
        return if (measureWidth < 0) 0 else measuredWidth
    }

    private fun isLayoutRtl(): Boolean  = layoutDirection == LAYOUT_DIRECTION_RTL

    private fun setPageSpacing(value: Int) {
        pageSpacingValue = value
    }
}