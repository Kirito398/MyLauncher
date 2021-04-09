package ru.biozzlab.mylauncher.ui.activity_contracts

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract

abstract class BaseAppWidgetContract<I> : ActivityResultContract<I, Int?>() {
    override fun parseResult(resultCode: Int, intent: Intent?): Int? {
        intent ?: return null
        if (resultCode != Activity.RESULT_OK) return null
        return intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1)
    }
}