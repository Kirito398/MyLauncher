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

    fun setup(launcher: Launcher, controller: DragController) {
        this.launcher = launcher
        this.controller = controller
    }

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
