package ru.biozzlab.mylauncher.di.modules

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import ru.biozzlab.mylauncher.view_models.DesktopViewModel
import ru.sir.presentation.annotations.ViewModelKey
import ru.sir.presentation.factories.ViewModelFactory

@Module
abstract class ViewModelModule {

    @Binds
    abstract fun bindViewModelFactory(factory: ViewModelFactory): ViewModelProvider.Factory

    @Binds
    @IntoMap
    @ViewModelKey(DesktopViewModel::class)
    abstract fun bindDesktopViewModel(desktopViewModel: DesktopViewModel): ViewModel
}