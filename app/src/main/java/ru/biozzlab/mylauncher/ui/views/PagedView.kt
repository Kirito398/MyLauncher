package ru.biozzlab.mylauncher.ui.views

import android.content.Context
import android.util.AttributeSet
import android.view.*
import android.widget.Scroller
import ru.biozzlab.mylauncher.domain.types.TouchStates
import java.lang.IllegalStateException
import kotlin.math.abs
import kotlin.math.sign

abstract class PagedView(context: Context, attrs: AttributeSet, defStyle: Int)
    : ViewGroup(context, attrs, defStyle) {

    constructor(context: Context, attrs: AttributeSet) : this(context, attrs, 0)

    companion object {
        const val NANO_TIME_DIV = 1000000000.0F
        const val SIGNIFICANT_MOVE_THRESHOLD = 0.4F
        const val RETURN_ORIGINAL_PAGE_THRESHOLD = 0.4F
        const val MIN_LENGTH_FOR_FLING = 25
        const val FLING_THRESHOLD_VELOCITY = 500
    }

    private val childRelativeOffset = mutableListOf<Int>()
    private var layoutScale = 1.0F
    private var nextPage = -1
    private var currentPage = 0
    private var pageSpacingValue: Int = 0
    private var isFirstLayout = true
    private var deferScrollUpdate = false

    private var touchState = TouchStates.REST
    private var activePointerId = -1
    private var lastMotionX = 0.0F
    private var downMotionX = 0.0F
    private var lastMotionXRemainder = 0.0F
    private var totalMotionX = 0.0F
    private var touchX = 0.0F
    private var smoothingTime = 0.0F

    private val scroller = Scroller(context)
    private var touchSlop: Int

    private var velocityTracker: VelocityTracker? = null
    private var maximumVelocity: Float
    private var flingThresholdVelocity: Float

    fun getNextPage() = if (nextPage != -1) nextPage else currentPage

    init {
        val count = childCount
        for (i in 0 .. count)
            childRelativeOffset.add(i, -1)

        val configuration = ViewConfiguration.get(context)
        touchSlop = configuration.scaledTouchSlop
        maximumVelocity = configuration.scaledMaximumFlingVelocity.toFloat()

        val density = resources.displayMetrics.density
        flingThresholdVelocity = FLING_THRESHOLD_VELOCITY * density
    }

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        if (childCount <= 0) return super.onInterceptTouchEvent(ev)

        val action = ev?.action ?: return true
        if (action == MotionEvent.ACTION_MOVE && touchState == TouchStates.SCROLLING) return true

        when (action) {
            MotionEvent.ACTION_DOWN -> {
                val x = ev.x
                val y = ev.y

                lastMotionX = x
                lastMotionXRemainder = 0.0F
                totalMotionX = 0.0F
                activePointerId = ev.getPointerId(0)

                val xDist = abs(scroller.finalX - scroller.currX)
                val finishedScrolling = scroller.isFinished || xDist < touchSlop
                if (finishedScrolling) {
                    touchState = TouchStates.REST
                    scroller.abortAnimation()
                } else {
                    touchState = TouchStates.SCROLLING
                }

                if (touchState != TouchStates.PREV_PAGE && touchState != TouchStates.NEXT_PAGE) {
                    touchState = when {
                        hitsPreviousPage(x, y) -> TouchStates.PREV_PAGE
                        hitsNextPage(x, y) -> TouchStates.NEXT_PAGE
                        else -> touchState
                    }
                }
            }
        }

        return super.onInterceptTouchEvent(ev)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        val action = event?.action ?: return true

        when (action) {
            MotionEvent.ACTION_DOWN -> {
                lastMotionX = event.x
                downMotionX = lastMotionX
                lastMotionXRemainder = 0.0F
                totalMotionX = 0.0F
                activePointerId = event.getPointerId(0)

                if (touchState == TouchStates.SCROLLING)
                    pageBeginMoving()
            }
            MotionEvent.ACTION_MOVE -> {
                if (touchState != TouchStates.SCROLLING)
                    return true

                val pointerIndex = event.findPointerIndex(activePointerId)
                val x = event.getX(pointerIndex)
                val deltaX = lastMotionX + lastMotionXRemainder - x

                totalMotionX += abs(deltaX)

                if (abs(deltaX) >= 1.0F) {
                    touchX += deltaX
                    smoothingTime = System.nanoTime() / NANO_TIME_DIV

                    if (!deferScrollUpdate)
                        scrollBy(deltaX.toInt(), 0)
                    else
                        invalidate()

                    lastMotionX = x
                    lastMotionXRemainder = deltaX - deltaX.toInt()
                } else {
                    awakenScrollBars()
                }
            }
            MotionEvent.ACTION_UP -> {
                when (touchState) {
                    TouchStates.REST -> {}
                    TouchStates.SCROLLING -> {
                        val pointerIndex = event.findPointerIndex(activePointerId)
                        val x = event.getX(pointerIndex)
                        velocityTracker?.computeCurrentVelocity(1000, maximumVelocity) ?: return true
                        val velocityX = velocityTracker?.xVelocity ?: return true
                        val deltaX = x - downMotionX
                        val pageWidth = getScaledMeasuredWidth(getChildAt(currentPage))
                        val isSignificantMove = abs(deltaX) > pageWidth * SIGNIFICANT_MOVE_THRESHOLD

                        totalMotionX += abs(lastMotionX + lastMotionXRemainder - x)

                        val isFling = totalMotionX > MIN_LENGTH_FOR_FLING && abs(velocityX) > flingThresholdVelocity
                        val returnToOriginalPage = abs(deltaX) > pageWidth * RETURN_ORIGINAL_PAGE_THRESHOLD && sign(velocityX) != sign(deltaX) && isFling
                        val finalPage: Int
                        val isRtl = isLayoutRtl()
                        val isDeltaXLeft = if (isRtl) deltaX > 0 else deltaX < 0
                        val isVelocityXLeft = if (isRtl) velocityX > 0 else velocityX < 0

                        when {
                            ((isSignificantMove && !isDeltaXLeft && !isFling) || (isFling && !isVelocityXLeft)) && currentPage > 0 -> {
                                finalPage = if (returnToOriginalPage) currentPage else currentPage - 1
                                snapToPageWithVelocity(finalPage, velocityX)
                            }
                            ((isSignificantMove && isDeltaXLeft && !isFling) || (isFling && isVelocityXLeft)) && currentPage < childCount - 1 -> {
                                finalPage = if (returnToOriginalPage) currentPage else currentPage + 1
                                snapToPageWithVelocity(finalPage, velocityX)
                            }
                            else -> snapToDestination()
                        }
                    }
                    TouchStates.PREV_PAGE -> TODO()
                    TouchStates.NEXT_PAGE -> TODO()
                }
            }
            MotionEvent.ACTION_CANCEL -> {
                if (touchState == TouchStates.SCROLLING)
                    snapToDestination()

                touchState = TouchStates.REST
                activePointerId = -1
                releaseVelocityTracker()
            }
        }
        return true
    }

    private fun releaseVelocityTracker() {
        velocityTracker?.recycle()
        velocityTracker = null
    }

    private fun snapToDestination() {
        //TODO("Not yet implemented")
    }

    private fun snapToPageWithVelocity(page: Int, velocityX: Float) {
        //TODO("Not yet implemented")
    }

    private fun pageBeginMoving() {
        //TODO("Not yet implemented")
    }

    private fun hitsNextPage(x: Float, y: Float): Boolean {
        return if (isLayoutRtl())
            (x < getRelativeChildOffset(currentPage) - pageSpacingValue)
        else
            (x > (measuredWidth - getRelativeChildOffset(currentPage)) + pageSpacingValue)
    }

    private fun hitsPreviousPage(x: Float, y: Float): Boolean {
        return if (isLayoutRtl())
            (x > (measuredWidth - getRelativeChildOffset(currentPage)) + pageSpacingValue)
        else
            (x < getRelativeChildOffset(currentPage) - pageSpacingValue)
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