package ru.biozzlab.mylauncher.ui.layouts.params

import androidx.constraintlayout.widget.ConstraintLayout

class DragLayerParams(width: Int, height: Int) : ConstraintLayout.LayoutParams(width, height) {
    var x: Int = -1
    var y: Int = -1
    var customPosition = false
}