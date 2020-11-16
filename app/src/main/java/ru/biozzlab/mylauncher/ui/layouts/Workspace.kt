package ru.biozzlab.mylauncher.ui.layouts

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import ru.biozzlab.mylauncher.ui.views.PagedView
import java.lang.IllegalArgumentException

class Workspace(context: Context, attributeSet: AttributeSet, defStyle: Int) : PagedView(context, attributeSet, defStyle), View.OnTouchListener {

    constructor(context: Context, attributeSet: AttributeSet) : this(context, attributeSet, 0)

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        computeScroll()
    }

    override fun onChildViewAdded(parent: View?, child: View?) {
        if (child !is CellLayout)
            throw IllegalArgumentException("A workspace can only have CellLayout children!")

        child.setOnInterceptTouchListener(this)
    }

    override fun onChildViewRemoved(parent: View?, child: View?) {
        //TODO("Not yet implemented")
    }

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        return false
    }
}