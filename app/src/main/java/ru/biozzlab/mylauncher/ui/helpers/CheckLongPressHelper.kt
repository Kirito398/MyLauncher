package ru.biozzlab.mylauncher.ui.helpers

import android.view.View
import kotlinx.coroutines.*

class CheckLongPressHelper(private val view: View) {
    private val mainContext = Dispatchers.Main

    private var hasPerformedLongPress = false
    private var parentJob: Job = Job()

    fun checkForLongPress() {
        hasPerformedLongPress = false
        cancelLongPress()
        parentJob = Job()

        CoroutineScope(mainContext + parentJob).launch {
            delay(700)

            if (view.parent != null && view.hasWindowFocus() && !hasPerformedLongPress) {
                if (view.performLongClick()) {
                    view.isPressed = false
                    hasPerformedLongPress = true
                }
            }
        }
    }

    fun cancelLongPress() {
        hasPerformedLongPress = false
        parentJob.apply {
            cancelChildren()
            cancel()
        }
    }

    fun hasPerformedLongPress() = hasPerformedLongPress
}