package ru.biozzlab.mylauncher.ui.activity_contracts

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent

class AppWidgetBindContract : BaseAppWidgetContract<Pair<Int, ComponentName>>() {

    override fun createIntent(context: Context, input: Pair<Int, ComponentName>?) =
        Intent(AppWidgetManager.ACTION_APPWIDGET_BIND)
            .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, input?.first)
            .putExtra(AppWidgetManager.EXTRA_APPWIDGET_PROVIDER, input?.second)
}