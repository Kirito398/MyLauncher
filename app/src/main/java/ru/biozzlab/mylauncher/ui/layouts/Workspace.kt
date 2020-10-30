package ru.biozzlab.mylauncher.ui.layouts

import android.content.Context
import android.util.AttributeSet
import ru.biozzlab.mylauncher.ui.views.PagedView

class Workspace(context: Context, attributeSet: AttributeSet, defStyle: Int) : PagedView(context, attributeSet, defStyle) {

    constructor(context: Context, attributeSet: AttributeSet) : this(context, attributeSet, 0)

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        computeScroll()
    }
}