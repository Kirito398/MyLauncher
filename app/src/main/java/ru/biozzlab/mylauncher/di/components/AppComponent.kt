package ru.biozzlab.mylauncher.di.components

import dagger.Component
import ru.biozzlab.mylauncher.di.modules.CacheModule
import ru.biozzlab.mylauncher.di.modules.DataModule
import ru.biozzlab.mylauncher.di.modules.DomainModule
import ru.biozzlab.mylauncher.di.modules.PresentationModule
import ru.biozzlab.mylauncher.ui.Launcher
import javax.inject.Singleton

@Singleton
@Component(modules = [PresentationModule::class, DomainModule::class, DataModule::class, CacheModule::class])
interface AppComponent {
    fun injectLauncher(launcher: Launcher)
}