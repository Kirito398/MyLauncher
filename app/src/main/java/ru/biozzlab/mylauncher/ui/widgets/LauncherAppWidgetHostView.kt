package ru.biozzlab.mylauncher.ui.widgets

import android.annotation.SuppressLint
import android.appwidget.AppWidgetHostView
import android.content.Context
import android.view.MotionEvent
import android.view.ViewGroup
import ru.biozzlab.mylauncher.ui.helpers.CheckLongPressHelper

class LauncherAppWidgetHostView(context: Context?) : AppWidgetHostView(context) {
    private val longPressHelper = CheckLongPressHelper(this)

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        if (longPressHelper.hasPerformedLongPress()) {
            longPressHelper.cancelLongPress()
            return true
        }

        return when (ev?.action) {
            MotionEvent.ACTION_DOWN -> {
                longPressHelper.checkForLongPress()
                true
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                longPressHelper.cancelLongPress()
                false
            }
            else -> false
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (longPressHelper.hasPerformedLongPress()) {
            longPressHelper.cancelLongPress()
            return true
        }

        return when (event?.action) {
            MotionEvent.ACTION_DOWN -> { true }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                longPressHelper.cancelLongPress()
                false
            }
            else -> false
        }
    }

    override fun cancelLongPress() {
        super.cancelLongPress()
        longPressHelper.cancelLongPress()
    }

    override fun getDescendantFocusability(): Int = ViewGroup.FOCUS_BLOCK_DESCENDANTS
}