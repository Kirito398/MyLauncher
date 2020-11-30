package ru.biozzlab.mylauncher.ui.layouts

import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import ru.biozzlab.mylauncher.App
import ru.biozzlab.mylauncher.R
import ru.biozzlab.mylauncher.controllers.DragController
import ru.biozzlab.mylauncher.easyLog
import ru.biozzlab.mylauncher.ui.Launcher
import kotlin.math.round

class DragLayer(context: Context, attrs: AttributeSet) : ConstraintLayout(context, attrs) {
    private lateinit var launcher: Launcher
    private lateinit var controller: DragController
//
//    private val leftHoverDrawable: Drawable = resources.getDrawable(R.drawable.page_hover_left_holo, null)
//    private val rightHoverDrawable: Drawable = resources.getDrawable(R.drawable.page_hover_right_holo, null)

    fun setup(launcher: Launcher, controller: DragController) {
        this.launcher = launcher
        this.controller = controller
    }

//    override fun onTouchEvent(event: MotionEvent?): Boolean {
//        event?.run {
//            if (action == MotionEvent.ACTION_DOWN)
//                if (handleTouchDown(this, false))
//                    return true
//            return controller.onTouchEvent(this)
//        } ?: return false
//    }
//
//    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
//        "On intercept touch event".easyLog(this.javaClass.simpleName)
//        ev?.run {
//            if (action == MotionEvent.ACTION_DOWN)
//                if (handleTouchDown(this, true))
//                    return true
//        }
//
//        return super.onInterceptTouchEvent(ev)
//    }

//    private fun handleTouchDown(ev: MotionEvent, intercept: Boolean): Boolean {
//        val hitRect = Rect()
//        val x = ev.x
//        val y = ev.y
//
//        requestDisallowInterceptTouchEvent(true)
//
//        return true
//    }

//    override fun dispatchDraw(canvas: Canvas?) {
//        super.dispatchDraw(canvas)
//
//        if (canvas == null) return
//
//        if (!App.isScreenLarge) {
//            val workspace = launcher.workspace
//            val width = workspace.width
//            val childRect = Rect()
//
//            getDescendantRectRelativeToSelf(workspace.getChildAt(0), childRect)
//
//            val page = workspace.getNextPage()
//            val isRtl = isLayoutDirectionRtl()
//            val leftPage = workspace.getChildAt(if (isRtl) page + 1 else page - 1) as CellLayout?
//            val rightPage = workspace.getChildAt(if (isRtl) page - 1 else page + 1) as CellLayout?
//
//            leftPage?.run {
//                if (!this.getIsDragOverlapping()) return
//                leftHoverDrawable.setBounds(0, childRect.top, leftHoverDrawable.intrinsicWidth, childRect.bottom)
//                leftHoverDrawable.draw(canvas)
//            } ?: rightPage?.run {
//                if (!this.getIsDragOverlapping()) return
//                rightHoverDrawable.setBounds(width - rightHoverDrawable.intrinsicWidth, childRect.top, width, childRect.bottom)
//                rightHoverDrawable.draw(canvas)
//            }
//        }
//    }

//    private fun isLayoutDirectionRtl(): Boolean = layoutDirection == LAYOUT_DIRECTION_RTL

//    private fun getDescendantRectRelativeToSelf(descendant: View, childRect: Rect): Float {
//        val tempX = 0F
//        val tempY = 0F
//        val scale = getDescendantCordRelativeToSelf(descendant, mutableListOf(tempX, tempY))
//        childRect.set(tempX, tempY, tempX + descendant.width, tempY + descendant.height)
//        return scale
//    }

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        return ev?.let { controller.onInterceptTouchEvent(it, this) } ?: false
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        return event?.let { controller.onTouchEvent(it, this) } ?: false
    }

    fun getLocationInDragLayer(view: View, location: MutableList<Float>): Float {
        location.add(0, 0F)
        location.add(1, 0F)
        return getDescendantCordRelativeToSelf(view, location)
    }

    private fun getDescendantCordRelativeToSelf(descendant: View, point: MutableList<Float>): Float {
        var scale = 1.0F
        descendant.matrix.mapPoints(point.toFloatArray())
        scale *= descendant.scaleX
        point[0] = descendant.left + point[0]
        point[1] = descendant.top + point[1]

        var parent = descendant.parent
        while (parent is View && parent != this) {
            val view = parent
            view.matrix.mapPoints(point.toFloatArray())
            scale *= view.scaleX
            point[0] = point[0] + (view.left - view.scrollX)
            point[1] = point[1] + (view.top - view.scrollY)
            parent = view.getParent()
        }

        point[0] = round(point[0])
        point[1] = round(point[1])

        return scale
    }
}
