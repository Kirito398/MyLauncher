package ru.biozzlab.mylauncher.ui.widgets

import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetHostView
import android.appwidget.AppWidgetProviderInfo
import android.content.Context

class LauncherAppWidgetHost(context: Context?, hostId: Int) : AppWidgetHost(context, hostId) {

    override fun onCreateView(
        context: Context?,
        appWidgetId: Int,
        appWidget: AppWidgetProviderInfo?
    ): AppWidgetHostView {
        return LauncherAppWidgetHostView(context)
    }

    override fun stopListening() {
        super.stopListening()
        clearViews()
    }
}