package ru.biozzlab.mylauncher.ui.views

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.view.View
import ru.biozzlab.mylauncher.ui.layouts.DragLayer
import ru.biozzlab.mylauncher.ui.layouts.params.DragLayerParams

class DragView(context: Context) : View(context) {

    private lateinit var bitmap: Bitmap
    private lateinit var paint: Paint
    private lateinit var dragLayer: DragLayer
    private var registrationX = -1
    private var registrationY = -1

    lateinit var dragRegion: Rect

    constructor(context: Context, dragLayer: DragLayer, bitmap: Bitmap, registrationX: Int, registrationY: Int, left: Int, top: Int, width: Int, height: Int, initialScale: Float): this(context) {
        scaleX = initialScale
        scaleY = initialScale

        this.bitmap = Bitmap.createBitmap(bitmap, left, top, width, height)
        dragRegion = Rect(0, 0, width, height)

        this.dragLayer = dragLayer
        this.registrationX = registrationX
        this.registrationY = registrationY

        paint = Paint(Paint.FILTER_BITMAP_FLAG)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        setMeasuredDimension(bitmap.width, bitmap.height)
    }

    override fun onDraw(canvas: Canvas?) {
        canvas?.drawBitmap(bitmap, 0.0F, 0.0F, paint)
    }

    fun show(motionDownX: Int, motionDownY: Int) {
        dragLayer.addView(this)

        val layoutParams = DragLayerParams(0, 0)
        layoutParams.width = bitmap.width
        layoutParams.height = bitmap.height
        layoutParams.customPosition = true

        setLayoutParams(layoutParams)
        move(motionDownX, motionDownY)
    }

    fun move(toX: Int, toY: Int) {
        translationX = (toX - registrationX).toFloat()
        translationY = (toY - registrationY).toFloat()
    }

    fun remove() {
        parent?.let { dragLayer.removeView(this) }
    }
}