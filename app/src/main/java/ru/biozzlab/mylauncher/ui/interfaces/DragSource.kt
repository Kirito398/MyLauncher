package ru.biozzlab.mylauncher.ui.interfaces

import android.view.View
import ru.biozzlab.mylauncher.domain.models.DragObject

interface DragSource {
    fun onDropComplete(target: View, dragObject: DragObject, isFlingToDelete: Boolean, success: Boolean)
}