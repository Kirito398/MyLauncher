package ru.biozzlab.mylauncher.ui.activity_contracts

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent

class AppWidgetPickContract : BaseAppWidgetContract<Int>() {

    override fun createIntent(context: Context, input: Int?) =
        Intent(AppWidgetManager.ACTION_APPWIDGET_PICK).
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, input)
}