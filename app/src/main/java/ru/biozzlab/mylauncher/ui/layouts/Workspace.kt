package ru.biozzlab.mylauncher.ui.layouts

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import ru.biozzlab.mylauncher.ui.views.PagedView

class Workspace(context: Context, attributeSet: AttributeSet, defStyle: Int) : PagedView(context, attributeSet, defStyle) {

    constructor(context: Context, attributeSet: AttributeSet) : this(context, attributeSet, 0)

    override fun syncPageItems(i: Int, b: Boolean) {
        //TODO("Not yet implemented")
    }

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        return super.onInterceptTouchEvent(ev)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        computeScroll()
    }
}