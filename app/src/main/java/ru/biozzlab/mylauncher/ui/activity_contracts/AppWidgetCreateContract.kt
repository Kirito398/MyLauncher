package ru.biozzlab.mylauncher.ui.activity_contracts

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent

class AppWidgetCreateContract : BaseAppWidgetContract<Pair<ComponentName, Int>>() {
    override fun createIntent(context: Context, input: Pair<ComponentName, Int>?) =
        Intent(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE).apply {
            component = input?.first
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, input?.second)
        }
}