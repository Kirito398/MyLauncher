package ru.biozzlab.mylauncher.ui.widgets

import android.appwidget.AppWidgetHostView
import android.content.Context
import android.view.MotionEvent
import ru.biozzlab.mylauncher.ui.helpers.CheckLongPressHelper

class LauncherAppWidgetHostView(context: Context?) : AppWidgetHostView(context) {
    private val longPressHelper = CheckLongPressHelper(this)

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        if (longPressHelper.hasPerformedLongPress()) {
            longPressHelper.cancelLongPress()
            return true
        }

        when (ev?.action) {
            MotionEvent.ACTION_DOWN -> { longPressHelper.checkForLongPress() }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> { longPressHelper.cancelLongPress() }
        }

        return false
    }

    override fun cancelLongPress() {
        super.cancelLongPress()
        longPressHelper.cancelLongPress()
    }
}