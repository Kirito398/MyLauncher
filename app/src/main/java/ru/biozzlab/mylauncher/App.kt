package ru.biozzlab.mylauncher

import android.app.Application

class App : Application() {
    companion object {
        var isScreenLarge: Boolean = false
    }

    override fun onCreate() {
        super.onCreate()

        isScreenLarge = resources.getBoolean(R.bool.is_large_screen)
    }
}