package ru.biozzlab.mylauncher.ui.views

import android.content.Context
import android.util.AttributeSet
import android.view.*
import android.widget.Scroller
import ru.biozzlab.mylauncher.domain.types.TouchStates
import ru.biozzlab.mylauncher.ui.layouts.interfaces.Page
import kotlin.math.*

abstract class PagedView(context: Context, attrs: AttributeSet, defStyle: Int)
    : ViewGroup(context, attrs, defStyle), ViewGroup.OnHierarchyChangeListener {

    constructor(context: Context, attrs: AttributeSet) : this(context, attrs, 0)

    companion object {
        const val PAGE_SNAP_ANIMATION_DURATION = 550
    }

    private val childOffset = mutableListOf<Int>()
    private val childRelativeOffset = mutableListOf<Int>()
    private val childOffsetWithLayoutScale = mutableListOf<Int>()

    private var layoutScale = 1.0F
    private var nextPage = -1
    private var currentPage = 0
    private var pageSpacingValue: Int = 0
    private var isFirstLayout = true

    private var touchState = TouchStates.REST
    private var activePointerId = -1
    private var lastMotionX = 0.0F
    private var downMotionX = 0.0F
    private var totalMotionX = 0.0F
    private var unboundedScrollX = 0

    private val scroller = Scroller(context)
    private var touchSlop: Int


    fun getNextPage() = if (nextPage != -1) nextPage else currentPage

    init {
        isHapticFeedbackEnabled = false

        invalidateCachedOffsets()

        val configuration = ViewConfiguration.get(context)
        touchSlop = configuration.scaledTouchSlop
    }

    private fun invalidateCachedOffsets() {
        childOffset.clear()
        childRelativeOffset.clear()
        childOffsetWithLayoutScale.clear()

        val count = childCount
        for (i in 0 .. count) {
            childOffset.add(i, -1)
            childRelativeOffset.add(i, -1)
            childOffsetWithLayoutScale.add(i, -1)
        }
    }

    override fun onChildViewAdded(parent: View?, child: View?) {
        invalidate()
        invalidateCachedOffsets()
    }

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        if (childCount <= 0)
            return super.onInterceptTouchEvent(ev)

        val action = ev?.action ?: return false

        if (action == MotionEvent.ACTION_MOVE && touchState == TouchStates.SCROLLING) return true

        when (action) {
            MotionEvent.ACTION_DOWN -> initScrolling(ev)
            MotionEvent.ACTION_MOVE -> scrollingStart(ev)
        }

        return touchState != TouchStates.REST
    }

    private fun initScrolling(ev: MotionEvent) {
        val x = ev.x

        lastMotionX = x
        totalMotionX = 0.0F
        activePointerId = ev.getPointerId(0)

        val xDist = abs(scroller.finalX - scroller.currX)
        val isFinishedScrolling = scroller.isFinished || xDist < touchSlop

        if (isFinishedScrolling) {
            touchState = TouchStates.REST
            scroller.abortAnimation()
        } else {
            touchState = TouchStates.SCROLLING
        }
    }

    private fun scrollingStart(ev: MotionEvent, touchSlopScale: Float = 1.0F) {
        if (activePointerId == -1) return

        val pointerIndex = ev.findPointerIndex(activePointerId)
        if (pointerIndex == -1) return

        val x = ev.getX(pointerIndex)
        val xDiff = abs(x - lastMotionX)

        val touchSlop = round(touchSlopScale * touchSlop)
        val xMoved = xDiff > touchSlop

        if (xMoved) {
            touchState = TouchStates.SCROLLING
            totalMotionX += abs(lastMotionX - x)
            lastMotionX = x
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        val action = event?.action ?: return false

        return when (action) {
            MotionEvent.ACTION_DOWN -> {
                downMotionX = lastMotionX
                return true
            }
            MotionEvent.ACTION_MOVE -> scrolling(event)
            MotionEvent.ACTION_UP -> snapToDestination()
            else -> true
        }
    }

    private fun snapToDestination(): Boolean {
        snapToPage(getPageNearestToCenterOfScreen())
        return true
    }

    private fun getPageNearestToCenterOfScreen(): Int {
        var minDistanceFromScreenCenter = Integer.MAX_VALUE
        var nearestPage = -1

        val screenCenter = scrollX + measuredWidth / 2
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            val childWidth = getScaledMeasuredWidth(child)
            val halfChildWidth = childWidth / 2
            val childCenter = getChildOffset(i) + halfChildWidth
            val distanceFromScreenCenter = abs(childCenter - screenCenter)

            if (distanceFromScreenCenter < minDistanceFromScreenCenter) {
                minDistanceFromScreenCenter = distanceFromScreenCenter
                nearestPage = i
            }
        }

        return nearestPage
    }

    private fun snapToPage(page: Int) {
        val toPage = max(0, min(page, childCount - 1))
        val newX = getChildOffset(toPage) - getRelativeChildOffset(toPage)
        val sX = unboundedScrollX
        val delta = newX - sX
        snapToPage(toPage, delta)
    }

    private fun snapToPage(page: Int, delta: Int, duration: Int = PAGE_SNAP_ANIMATION_DURATION) {
        if (!scroller.isFinished) scroller.abortAnimation()
        scroller.startScroll(unboundedScrollX, 0, delta, 0, duration)
        invalidate()
    }

    override fun scrollTo(x: Int, y: Int) {
        super.scrollTo(x, y)
        unboundedScrollX = x
    }

    override fun computeScroll() {
        computeScrollHelper()
    }

    private fun computeScrollHelper(): Boolean {
        if (scroller.computeScrollOffset()) {
            if (scrollX != scroller.currX || scrollY != scroller.currY) scrollTo(scroller.currX, scroller.currY)
            invalidate()
            return true
        }
        return false
    }

    private fun scrolling(event: MotionEvent): Boolean {
        if (touchState != TouchStates.SCROLLING) return false

        val pointerIndex = event.findPointerIndex(activePointerId)
        val x = event.getX(pointerIndex)
        val deltaX = lastMotionX - x

        totalMotionX += abs(deltaX)

        if (abs(deltaX) >= 1.0F) {
            scrollBy(deltaX.toInt(), 0)
            lastMotionX = x
        }

        return true
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

            child.layout(childLeft, childTop, childLeft + child.measuredWidth, childTop + childHeight)
            childLeft += childWidth + pageSpacingValue
        }

        if (isFirstLayout && currentPage >= 0 && currentPage < childCount) {
            isHorizontalScrollBarEnabled = false
            isHorizontalScrollBarEnabled = true
            isFirstLayout = false
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

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

        invalidateCachedOffsets()
    }


    private fun getScaledMeasuredWidth(child: View): Int {
        val measuredWidth = child.measuredWidth
        val maxWidth = if (measuredWidth < 0) 0 else measuredWidth
        return (maxWidth * layoutScale + 0.5F).toInt()
    }

    private fun getChildOffset(index: Int): Int {
        val isRtl = isLayoutRtl()
        val childOffsets = if (layoutScale.compareTo(1.0F) == 0) childOffset else childOffsetWithLayoutScale

        if (childOffsets.isNotEmpty() && childOffsets[index] != -1) return childOffsets[index]
        if (childCount == 0) return 0

        val startIndex = if (isRtl) childCount - 1 else 0
        val delta = if (isRtl) -1 else 1

        var offset = getRelativeChildOffset(startIndex)
        for (i in startIndex until index step delta)
            offset += getScaledMeasuredWidth(getChildAt(i)) + pageSpacingValue

        if (childOffsets.isNotEmpty())
            childOffsets[index] = offset

        return offset
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
}