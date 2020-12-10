package ru.biozzlab.mylauncher

import android.app.Application
import android.content.Context
import ru.biozzlab.mylauncher.di.components.AppComponent
import ru.biozzlab.mylauncher.di.components.DaggerAppComponent
import ru.biozzlab.mylauncher.di.modules.CacheModule

class App : Application() {
    companion object {
        const val WORKSPACE_IS_INIT = "workspace_is_init"

        var isScreenLarge: Boolean = false
        lateinit var appContext: Context
        lateinit var appComponent: AppComponent
    }

    override fun onCreate() {
        super.onCreate()

        appContext = applicationContext
        isScreenLarge = resources.getBoolean(R.bool.is_large_screen)

        initAppComponent()
    }

    private fun initAppComponent() {
        appComponent = DaggerAppComponent.builder().cacheModule(CacheModule(applicationContext)).build()
    }
}