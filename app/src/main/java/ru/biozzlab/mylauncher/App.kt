package ru.biozzlab.mylauncher

import android.app.Application
import android.content.Context

class App : Application() {
    companion object {
        var isScreenLarge: Boolean = false
        lateinit var appContext: Context
    }

    override fun onCreate() {
        super.onCreate()

        appContext = applicationContext
        isScreenLarge = resources.getBoolean(R.bool.is_large_screen)
    }
}