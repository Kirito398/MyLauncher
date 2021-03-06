package ru.biozzlab.mylauncher.ui.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.item_hotseat.view.*
import ru.biozzlab.mylauncher.R
import ru.biozzlab.mylauncher.R.drawable.all_apps_button_icon
import ru.biozzlab.mylauncher.easyLog

class HotSeat : FrameLayout {
    constructor(context: Context) : this(context, null)
    constructor(context: Context, attributeSet: AttributeSet?) : this(context, attributeSet, 0)
    constructor(context: Context, attributeSet: AttributeSet?, defStyle: Int) : super(context, attributeSet, defStyle) {
        val attrs = context.obtainStyledAttributes(attributeSet, R.styleable.HotSeat, defStyle, 0)

        cellCountX = attrs.getInt(R.styleable.HotSeat_cellCountX, -1)
        cellCountY = attrs.getInt(R.styleable.HotSeat_cellCountY, -1)
    }

    private val cellCountX: Int
    private val cellCountY: Int
    //private val allAppsButtonRank: Int


    override fun onFinishInflate() {
        super.onFinishInflate()
        hotSeatContent.setGridSize(cellCountX, cellCountY)
        hotSeatContent.setIsHotSeat(true)

        resetLayout()
    }

    private fun resetLayout() {
        hotSeatContent.removeAllViewsInLayout()

        val inflater = LayoutInflater.from(context)
        val allAppsButton = inflater.inflate(R.layout.item_application, hotSeatContent, false) as TextView
        allAppsButton.setCompoundDrawablesWithIntrinsicBounds(null, ContextCompat.getDrawable(context, all_apps_button_icon), null, null)
        allAppsButton.contentDescription = "Apps"

        allAppsButton.setOnClickListener { "OnAllAppsButtonClicked!".easyLog(this::class.java.simpleName) }

        val x = 2
        val y = 0
        val params = CellLayout.LayoutParams(x, y, 1, 1)
        hotSeatContent.addViewToCell(allAppsButton, -1, 0, params, true)
    }
}