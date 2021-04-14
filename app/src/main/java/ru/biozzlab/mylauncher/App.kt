package ru.biozzlab.mylauncher

import android.content.Context
import ru.biozzlab.mylauncher.di.components.AppComponent
import ru.biozzlab.mylauncher.di.components.DaggerAppComponent
import ru.biozzlab.mylauncher.di.modules.CacheModule
import ru.sir.presentation.base.BaseApplication
import ru.sir.presentation.base.BaseDaggerComponent
import java.lang.IllegalArgumentException

class App : BaseApplication() {
    companion object {
        var isScreenLarge: Boolean = false
        lateinit var appContext: Context
        lateinit var appComponent: AppComponent
    }

    override fun provideComponent(type: Class<out BaseDaggerComponent>): BaseDaggerComponent {
        return when(type) {
            AppComponent::class.java -> appComponent
            else -> throw IllegalArgumentException("Dagger component not provided: $type")
        }
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