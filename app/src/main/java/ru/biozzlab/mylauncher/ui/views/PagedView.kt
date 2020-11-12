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
        const val NANO_TIME_DIV = 1000000000.0F
        const val SIGNIFICANT_MOVE_THRESHOLD = 0.4F
        const val RETURN_ORIGINAL_PAGE_THRESHOLD = 0.4F
        const val MIN_LENGTH_FOR_FLING = 25
        const val FLING_THRESHOLD_VELOCITY = 500
        const val MIN_FLING_VELOCITY = 250
        const val MIN_SNAP_VELOCITY = 1500
        const val PAGE_SNAP_ANIMATION_DURATION = 550
        const val MAX_PAGE_SNAP_DURATION = 750
    }

    private val childOffset = mutableListOf<Int>()
    private val childRelativeOffset = mutableListOf<Int>()
    private val childOffsetWithLayoutScale = mutableListOf<Int>()
    private val dirtyPageContent = mutableListOf<Boolean>()
    private var layoutScale = 1.0F
    private var nextPage = -1
    private var currentPage = 0
    private var pageSpacingValue: Int = 0
    private var isFirstLayout = true
    private var deferScrollUpdate = false
    private var deferLoadAssociatedPagesUntilScrollCompletes = false
    private var contentISRefreshable = true
    private var allowOverScroll = true

    private var touchState = TouchStates.REST
    private var activePointerId = -1
    private var lastMotionX = 0.0F
    private var downMotionX = 0.0F
    private var lastMotionXRemainder = 0.0F
    private var totalMotionX = 0.0F
    private var touchX = 0.0F
    private var smoothingTime = 0.0F
    private var unboundedScroll = 0
    private var maxScrollX = 0
    private var overScrollX = 0

    private val scroller = Scroller(context)
    private var touchSlop: Int

    private var velocityTracker: VelocityTracker? = null
    private var maximumVelocity: Float
    private var flingThresholdVelocity: Float
    private var minFlingVelocity: Float
    private var minSnapVelocity: Float

    fun getNextPage() = if (nextPage != -1) nextPage else currentPage

    init {
        isHapticFeedbackEnabled = false

        invalidateCachedOffsets()

        val configuration = ViewConfiguration.get(context)
        touchSlop = configuration.scaledTouchSlop
        maximumVelocity = configuration.scaledMaximumFlingVelocity.toFloat()

        val density = resources.displayMetrics.density
        flingThresholdVelocity = FLING_THRESHOLD_VELOCITY * density
        minFlingVelocity = MIN_FLING_VELOCITY * density
        minSnapVelocity = MIN_SNAP_VELOCITY * density
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

        val action = ev?.action
            ?: return false

        initVelocityTracker(ev)

        if (action == MotionEvent.ACTION_MOVE && touchState == TouchStates.SCROLLING)
            return true

        when (action) {
            MotionEvent.ACTION_MOVE -> {
                if (activePointerId == -1)
                    return touchState != TouchStates.REST
                determineScrollingStart(ev)
            }

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

            MotionEvent.ACTION_UP,
            MotionEvent.ACTION_CANCEL -> {
                touchState = TouchStates.REST
                activePointerId - 1
                releaseVelocityTracker()
            }

            MotionEvent.ACTION_POINTER_UP -> {
                onSecondaryPointerUp(ev)
                releaseVelocityTracker()
            }
        }

        return touchState != TouchStates.REST
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        val action = event?.action ?: return false

        initVelocityTracker(event)

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
                    return false

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
                    TouchStates.REST -> { }
                    TouchStates.SCROLLING -> {
                        val pointerIndex = event.findPointerIndex(activePointerId)
                        val x = event.getX(pointerIndex)
                        velocityTracker?.computeCurrentVelocity(1000, maximumVelocity)
                            ?: return true
                        val velocityX = velocityTracker?.xVelocity ?: return true
                        val deltaX = x - downMotionX
                        val pageWidth = getScaledMeasuredWidth(getChildAt(currentPage))
                        val isSignificantMove = abs(deltaX) > pageWidth * SIGNIFICANT_MOVE_THRESHOLD

                        totalMotionX += abs(lastMotionX + lastMotionXRemainder - x)

                        val isFling =
                            totalMotionX > MIN_LENGTH_FOR_FLING && abs(velocityX) > flingThresholdVelocity
                        val returnToOriginalPage =
                            abs(deltaX) > pageWidth * RETURN_ORIGINAL_PAGE_THRESHOLD && sign(
                                velocityX
                            ) != sign(deltaX) && isFling
                        val finalPage: Int
                        val isRtl = isLayoutRtl()
                        val isDeltaXLeft = if (isRtl) deltaX > 0 else deltaX < 0
                        val isVelocityXLeft = if (isRtl) velocityX > 0 else velocityX < 0

                        when {
                            ((isSignificantMove && !isDeltaXLeft && !isFling) || (isFling && !isVelocityXLeft)) && currentPage > 0 -> {
                                finalPage =
                                    if (returnToOriginalPage) currentPage else currentPage - 1
                                snapToPageWithVelocity(finalPage, velocityX)
                            }
                            ((isSignificantMove && isDeltaXLeft && !isFling) || (isFling && isVelocityXLeft)) && currentPage < childCount - 1 -> {
                                finalPage =
                                    if (returnToOriginalPage) currentPage else currentPage + 1
                                snapToPageWithVelocity(finalPage, velocityX)
                            }
                            else -> snapToDestination()
                        }
                    }
                    TouchStates.PREV_PAGE -> TODO()
                    TouchStates.NEXT_PAGE -> TODO()
                }
                touchState = TouchStates.REST
                activePointerId = -1
                releaseVelocityTracker()
            }
            MotionEvent.ACTION_CANCEL -> {
                if (touchState == TouchStates.SCROLLING)
                    snapToDestination()

                touchState = TouchStates.REST
                activePointerId = -1
                releaseVelocityTracker()
            }
            MotionEvent.ACTION_POINTER_UP -> onSecondaryPointerUp(event)
        }

        return true
    }

    private fun determineScrollingStart(ev: MotionEvent, touchSlopScale: Float = 1.0F) {
        val pointerIndex = ev.findPointerIndex(activePointerId)

        if (pointerIndex == -1) return

        val x = ev.getX(pointerIndex)
        val y = ev.getY(pointerIndex)
        val xDiff = abs(x - lastMotionX)
        //val diffY = abs(y - lastMotionY)

        val touchSlop = round(touchSlopScale * touchSlop)
        //val xPaged = xDiff > pagingTouchSlop
        val xMoved = xDiff > touchSlop

        if (xMoved) {
            touchState = TouchStates.SCROLLING
            totalMotionX += abs(lastMotionX - x)
            lastMotionX = x
            lastMotionXRemainder = 0.0F
            touchX = scrollX.toFloat()
            smoothingTime = System.nanoTime() / NANO_TIME_DIV
            pageBeginMoving()
            cancelCurrentPageLongPress()
        }
    }

    private fun cancelCurrentPageLongPress() {
        //TODO("Not yet implemented")
    }

    private fun initVelocityTracker(ev: MotionEvent) {
        if (velocityTracker == null)
            velocityTracker = VelocityTracker.obtain()
        velocityTracker?.addMovement(ev)
    }

    private fun onSecondaryPointerUp(ev: MotionEvent) {
        val pointerIndex = ev.action and MotionEvent.ACTION_POINTER_INDEX_MASK shr
                MotionEvent.ACTION_POINTER_INDEX_SHIFT

        val pointerId = ev.getPointerId(pointerIndex)
        if (pointerId != activePointerId) return

        val newPointerIndex = if (pointerIndex == 0) 1 else 0
        lastMotionX = ev.getX(newPointerIndex)
        downMotionX = lastMotionX
        lastMotionXRemainder = 0.0F
        activePointerId = ev.getPointerId(newPointerIndex)
        velocityTracker?.clear()
    }

    private fun releaseVelocityTracker() {
        velocityTracker?.recycle()
        velocityTracker = null
    }

    private fun snapToPageWithVelocity(page: Int, velocityX: Float) {
        val toPage = max(0, min(page, childCount - 1))
        val halfScreenSize = measuredWidth / 2

        val newX = getChildOffset(toPage) - getRelativeChildOffset(toPage)
        val delta = newX - unboundedScroll

        if (abs(velocityX) < minFlingVelocity) {
            snapToPage(toPage)
            return
        }

        val distanceRatio = min(1.0F, 1.0F * abs(delta) / (2 * halfScreenSize))
        val distance = halfScreenSize + halfScreenSize * distanceInfluenceForSnapDuration(
            distanceRatio
        )

        var velocity = abs(velocityX)
        velocity = max(minSnapVelocity, velocity)

        var duration = 4 * round(1000 * abs(distance / velocity)).toInt()
        duration = min(duration, MAX_PAGE_SNAP_DURATION)

        snapToPage(toPage, delta, duration)
    }

    private fun distanceInfluenceForSnapDuration(distanceRatio: Float): Float {
        var distance = distanceRatio - 0.5F
        distance = (distance * 0.3F * PI / 2.0F).toFloat()
        return sin(distance)
    }

    private fun snapToPage(page: Int, duration: Int = PAGE_SNAP_ANIMATION_DURATION) {
        val toPage = max(0, min(page, childCount - 1))
        val newX = getChildOffset(toPage) - getRelativeChildOffset(toPage)
        val sX = unboundedScroll
        val delta = newX - sX

        snapToPage(toPage, delta, duration)
    }

    private fun snapToPage(page: Int, delta: Int, duration: Int) {
        nextPage = page

        val focusedChild = focusedChild
        if (focusedChild != null && page != currentPage && focusedChild == getChildAt(currentPage))
            focusedChild.clearFocus()

        pageBeginMoving()
        awakenScrollBars(duration)

        var newDuration = duration
        if (newDuration == 0)
            newDuration = abs(delta)

        if (!scroller.isFinished) scroller.abortAnimation()
        scroller.startScroll(unboundedScroll, 0, delta, 0, newDuration)

        if (deferScrollUpdate)
            loadAssociatedPages(nextPage)
        else
            deferLoadAssociatedPagesUntilScrollCompletes = true

        notifyPageSwitchListener()
        invalidate()
    }

    private fun notifyPageSwitchListener() {
        //TODO("Not yet implemented")
    }

    private fun pageBeginMoving() {
        //TODO("Not yet implemented")
    }

    private fun loadAssociatedPages(page: Int, immediateAndOnly: Boolean = false) {
        if (!contentISRefreshable) return

        val pageCount = childCount
        if (page >= pageCount) return

        val lowerPageBound = getAssociatedLowerPageBound(page)
        val upperPageBound = getAssociatedUpperPageBound(page)

        for (i in 1 .. pageCount) {
            val layout = getChildAt(i) as Page
            if (i < lowerPageBound || i > upperPageBound) {
                if (layout.getPageChildCount() > 0)
                    layout.removeAllViewsOnPage()
                dirtyPageContent.add(i, true)
            }
        }

        for (i in 1 .. pageCount) {
            if (i != page && immediateAndOnly) continue

            if (i in lowerPageBound .. upperPageBound) {
                if (!dirtyPageContent[i]) continue
                syncPageItems(i, (i == page) && immediateAndOnly)
                dirtyPageContent[i] = false
            }
        }
    }

    abstract fun syncPageItems(i: Int, b: Boolean)

    private fun getAssociatedUpperPageBound(page: Int): Int =  min(page + 1, childCount - 1)

    private fun getAssociatedLowerPageBound(page: Int): Int = max(0, page - 1)

    private fun snapToDestination() {
        snapToPage(getPageNearestToCenterOfScreen())
    }

    private fun getPageNearestToCenterOfScreen(): Int {
        var minDistanceFromScreenCenter = Integer.MAX_VALUE
        var minDistanceFromScreenCenterIndex = -1
        val screenCenter = scrollX + measuredWidth / 2

        val count = childCount
        for (i in 1 until childCount) {
            val layout = getChildAt(i)
            val childWidth = getScaledMeasuredWidth(layout)
            val halfChildWidth = childWidth / 2
            val childCenter = getChildOffset(i) + halfChildWidth
            val distanceFromScreenCenter = abs(childCenter - screenCenter)
            if (distanceFromScreenCenter < minDistanceFromScreenCenter) {
                minDistanceFromScreenCenter = distanceFromScreenCenter
                minDistanceFromScreenCenterIndex = i
            }
        }

        return minDistanceFromScreenCenterIndex
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

        if (isFirstLayout && currentPage >= 0 && currentPage < childCount) {
            isHorizontalScrollBarEnabled = false
            updateCurrentPageScroll()
            isHorizontalScrollBarEnabled = true
            isFirstLayout = false
        }
    }

    private fun updateCurrentPageScroll() {
        val newX = if (currentPage in 0 .. childCount) {
            val offset = getChildOffset(currentPage)
            val relOffset = getRelativeChildOffset(currentPage)
            offset - relOffset
        } else 0

        scrollTo(newX, 0)
        scroller.finalX = newX
        scroller.forceFinished(true)
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

        invalidateCachedOffsets()
    }

    override fun scrollTo(x: Int, y: Int) {
        val isRtl = isLayoutRtl()
        unboundedScroll = x

        val isXBeforeFirstPage = if (isRtl) x > maxScrollX else x < 0
        val isXAfterLastPage = if (isRtl) (x < 0) else x > maxScrollX

        when {
            isXBeforeFirstPage -> {
                super.scrollTo(0, y)
                if (!allowOverScroll) return
                if (isRtl)
                    overScroll(x - maxScrollX)
                else
                    overScroll(x)
            }
            isXAfterLastPage -> {
                super.scrollTo(maxScrollX, y)
                if (!allowOverScroll) return
                if (isRtl)
                    overScroll(x)
                else
                    overScroll(x - maxScrollX)
            }
            else -> {
                overScrollX = x
                super.scrollTo(x, y)
            }
        }

        touchX = x.toFloat()
        smoothingTime = System.nanoTime() / NANO_TIME_DIV
    }

    private fun overScroll(i: Int) {
        //TODO("Not yet implemented")
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

    private fun setPageSpacing(value: Int) {
        pageSpacingValue = value
    }
}