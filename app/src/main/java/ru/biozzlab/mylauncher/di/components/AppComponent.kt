package ru.biozzlab.mylauncher.di.components

import dagger.Component
import ru.biozzlab.mylauncher.di.modules.*
import ru.biozzlab.mylauncher.ui.Launcher
import ru.biozzlab.mylauncher.ui.fragments.Desktop
import ru.sir.presentation.base.BaseDaggerComponent
import javax.inject.Singleton

@Singleton
@Component(modules = [ViewModelModule::class, PresentationModule::class, DomainModule::class, DataModule::class, CacheModule::class])
interface AppComponent : BaseDaggerComponent {
    fun injectLauncher(launcher: Launcher)
    fun injectDesktop(desktop: Desktop)
}